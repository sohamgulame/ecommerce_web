package com.Project1.project.mapper;

import com.Project1.project.dto.response.ReviewResponseDTO;
import com.Project1.project.entity.Review;

public class ReviewMapper {
    public static ReviewResponseDTO toResponse(Review r) {
        if (r == null) return null;
        Long userId = null; String userName = null;
        if (r.getUser() != null) {
            userId = r.getUser().getId();
            userName = r.getUser().getName();
        }
        return new ReviewResponseDTO(r.getId(), r.getProduct().getId(), userId, userName, r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
