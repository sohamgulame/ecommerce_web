package com.Project1.project.service;

import com.Project1.project.dto.request.ProductRequestDTO;
import com.Project1.project.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO request);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO request);

    ProductResponseDTO getProductById(Long id);

    Page<ProductResponseDTO> getAllProducts(Pageable pageable, Long categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice, Boolean onlyAvailable);

    ProductResponseDTO uploadProductImage(Long productId, org.springframework.web.multipart.MultipartFile file);

    ProductResponseDTO removeProductImage(Long productId, String imageUrl);

    void deleteProduct(Long id);
}
