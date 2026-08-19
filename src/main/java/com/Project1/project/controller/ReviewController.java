package com.Project1.project.controller;

import com.Project1.project.dto.request.ReviewRequestDTO;
import com.Project1.project.dto.response.ReviewResponseDTO;
import com.Project1.project.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/v1/reviews")
    public ResponseEntity<ReviewResponseDTO> addReview(@Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(reviewService.addReview(request));
    }

    @GetMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponseDTO>> productReviews(@PathVariable Long productId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewResponseDTO> page = reviewService.getReviewsForProduct(productId, pageable);
        return ResponseEntity.ok(page);
    }
}
