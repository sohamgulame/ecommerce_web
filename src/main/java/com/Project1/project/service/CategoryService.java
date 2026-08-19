package com.Project1.project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.Project1.project.dto.request.CategoryRequestDTO;
import com.Project1.project.dto.response.CategoryResponseDTO;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO request);

    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request);

    CategoryResponseDTO getCategoryById(Long id);

    Page<CategoryResponseDTO> getAllCategories(Pageable pageable);

    void deleteCategory(Long id);
}
