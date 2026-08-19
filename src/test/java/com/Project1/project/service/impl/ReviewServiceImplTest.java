package com.Project1.project.service.impl;

import com.Project1.project.dto.request.ReviewRequestDTO;
import com.Project1.project.entity.Product;
import com.Project1.project.exception.NotOrderedException;
import com.Project1.project.exception.ReviewAlreadyExistsException;
import com.Project1.project.repository.OrderRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.repository.ReviewRepository;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @InjectMocks private ReviewServiceImpl reviewService;

    @Test
    void addReview_rejectsDuplicateReview() {
        Product product = new Product();
        product.setId(10L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(currentUserProvider.getCurrentUserId()).thenReturn(3L);
        when(reviewRepository.existsByProductIdAndUserId(10L, 3L)).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.addReview(request()));
        verify(orderRepository, never()).findByUserId(anyLong(), any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_rejectsCustomerWhoDidNotOrderProduct() {
        Product product = new Product();
        product.setId(10L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(currentUserProvider.getCurrentUserId()).thenReturn(3L);
        when(reviewRepository.existsByProductIdAndUserId(10L, 3L)).thenReturn(false);
        when(orderRepository.findByUserId(eq(3L), any())).thenReturn(org.springframework.data.domain.Page.empty());

        assertThrows(NotOrderedException.class, () -> reviewService.addReview(request()));
        verify(reviewRepository, never()).save(any());
    }

    private ReviewRequestDTO request() {
        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setProductId(10L);
        request.setRating(5);
        request.setComment("Great product");
        return request;
    }
}
