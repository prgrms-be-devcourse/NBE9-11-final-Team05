package com.back.ovengers.domain.review.repository;

import com.back.ovengers.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
