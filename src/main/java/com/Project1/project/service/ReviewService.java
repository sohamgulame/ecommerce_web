package com.Project1.project.service;

import com.Project1.project.dto.request.ReviewRequestDTO;
import com.Project1.project.dto.response.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponseDTO addReview(ReviewRequestDTO request);
    Page<ReviewResponseDTO> getReviewsForProduct(Long productId, Pageable pageable);
}
