package com.Project1.project.service.impl;

import com.Project1.project.dto.request.ProductRequestDTO;
import com.Project1.project.dto.response.ProductResponseDTO;
import com.Project1.project.entity.Category;
import com.Project1.project.entity.Product;
import com.Project1.project.exception.CategoryNotFoundException;
import com.Project1.project.exception.ProductNotFoundException;
import com.Project1.project.mapper.ProductMapper;
import com.Project1.project.repository.CategoryRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.service.ProductService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final com.Project1.project.repository.ReviewRepository reviewRepository;
    private final com.Project1.project.service.FileStorageService fileStorageService;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              com.Project1.project.repository.ReviewRepository reviewRepository,
                              com.Project1.project.service.FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        Category cat = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));
        Product p = ProductMapper.toEntity(request, cat);
        Product saved = productRepository.save(p);
        return ProductMapper.toResponse(saved);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", key = "#id")
    })
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        Category cat = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStockQuantity(request.getStockQuantity());
        existing.setImageUrls(request.getImageUrls());
        existing.setCategory(cat);
        Product saved = productRepository.save(existing);
        return ProductMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public ProductResponseDTO getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        Double avg = reviewRepository.findAverageRatingByProductId(id);
        return ProductMapper.toResponse(p, avg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, Long categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice, Boolean onlyAvailable) {
        Specification<Product> spec = Specification.where(null);
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            ));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.ge(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.le(root.get("price"), maxPrice));
        }
        if (onlyAvailable != null && onlyAvailable) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0));
        }

        Page<Product> page = productRepository.findAll(spec, pageable);
        List<ProductResponseDTO> dtos = page.stream().map(p -> {
            Double avg = reviewRepository.findAverageRatingByProductId(p.getId());
            return ProductMapper.toResponse(p, avg);
        }).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", key = "#productId")
    })
    public ProductResponseDTO uploadProductImage(Long productId, org.springframework.web.multipart.MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        String imageUrl = fileStorageService.uploadImage(file, "products");
        product.getImageUrls().add(imageUrl);
        Product saved = productRepository.save(product);
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return ProductMapper.toResponse(saved, avg);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", key = "#productId")
    })
    public ProductResponseDTO removeProductImage(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        if (product.getImageUrls().remove(imageUrl)) {
            fileStorageService.deleteImage(imageUrl);
            Product saved = productRepository.save(product);
            Double avg = reviewRepository.findAverageRatingByProductId(productId);
            return ProductMapper.toResponse(saved, avg);
        }
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return ProductMapper.toResponse(product, avg);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", key = "#id")
    })
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}

