package com.Project1.project.service.impl;

import com.Project1.project.dto.request.ReviewRequestDTO;
import com.Project1.project.dto.response.ReviewResponseDTO;
import com.Project1.project.entity.Order;
import com.Project1.project.entity.OrderItem;
import com.Project1.project.entity.OrderStatus;
import com.Project1.project.entity.Product;
import com.Project1.project.entity.Review;
import com.Project1.project.entity.User;
import com.Project1.project.exception.NotOrderedException;
import com.Project1.project.exception.ReviewAlreadyExistsException;
import com.Project1.project.exception.ProductNotFoundException;
import com.Project1.project.mapper.ReviewMapper;
import com.Project1.project.repository.OrderRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.repository.ReviewRepository;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.service.ReviewService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final com.Project1.project.security.CurrentUserProvider currentUserProvider;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ProductRepository productRepository, OrderRepository orderRepository, UserRepository userRepository, com.Project1.project.security.CurrentUserProvider currentUserProvider) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", key = "#request.productId")
    })
    public ReviewResponseDTO addReview(ReviewRequestDTO request) {
        Product p = productRepository.findById(request.getProductId()).orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));

        Long userId = currentUserProvider.getCurrentUserId();
        if (reviewRepository.existsByProductIdAndUserId(p.getId(), userId)) {
            throw new ReviewAlreadyExistsException("User already reviewed this product");
        }

        // verify user has a valid (non-cancelled) order containing the product
        boolean ordered = orderRepository.findByUserId(userId, Pageable.unpaged()).stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .map(OrderItem::getProductId)
                .anyMatch(pid -> pid.equals(p.getId()));

        if (!ordered) throw new NotOrderedException("User has not ordered this product");

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Review r = new Review();
        r.setProduct(p);
        r.setUser(user);
        r.setRating(request.getRating());
        r.setComment(request.getComment());

        Review saved = reviewRepository.save(r);
        return ReviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsForProduct(Long productId, Pageable pageable) {
        // ensure product exists
        if (!productRepository.existsById(productId)) {
            throw new com.Project1.project.exception.ProductNotFoundException("Product not found: " + productId);
        }
        Page<Review> page = reviewRepository.findByProductId(productId, pageable);
        List<ReviewResponseDTO> dtos = page.stream().map(ReviewMapper::toResponse).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }
}
