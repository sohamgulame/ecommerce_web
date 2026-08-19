package com.Project1.project.mapper;

import com.Project1.project.dto.request.CategoryRequestDTO;
import com.Project1.project.dto.response.CategoryResponseDTO;
import com.Project1.project.entity.Category;

public class CategoryMapper {

    public static Category toEntity(CategoryRequestDTO dto) {
        if (dto == null) return null;
        Category c = new Category();
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        return c;
    }

    public static CategoryResponseDTO toResponse(Category entity) {
        if (entity == null) return null;
        return new CategoryResponseDTO(entity.getId(), entity.getName(), entity.getDescription());
    }
}
