package com.Project1.project.mapper;

import com.Project1.project.dto.request.ProductRequestDTO;
import com.Project1.project.dto.response.ProductResponseDTO;
import com.Project1.project.entity.Category;
import com.Project1.project.entity.Product;

import java.util.List;

public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto, Category category) {
        if (dto == null) return null;
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setImageUrls(dto.getImageUrls());
        p.setCategory(category);
        return p;
    }

    public static ProductResponseDTO toResponse(Product entity) {
        return toResponse(entity, null);
    }

    public static ProductResponseDTO toResponse(Product entity, Double averageRating) {
        if (entity == null) return null;
        String categoryName = null;
        Category c = entity.getCategory();
        if (c != null) categoryName = c.getName();
        List<String> imageUrls = entity.getImageUrls() != null 
                ? new java.util.ArrayList<>(entity.getImageUrls()) 
                : new java.util.ArrayList<>();
        return new ProductResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStockQuantity(),
                categoryName,
                imageUrls,
                averageRating
        );
    }
}
