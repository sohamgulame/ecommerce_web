package com.Project1.project.repository;

import com.Project1.project.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
    boolean existsByProductIdAndUserId(Long productId, Long userId);

    @org.springframework.data.jpa.repository.Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double findAverageRatingByProductId(@org.springframework.data.repository.query.Param("productId") Long productId);
}

