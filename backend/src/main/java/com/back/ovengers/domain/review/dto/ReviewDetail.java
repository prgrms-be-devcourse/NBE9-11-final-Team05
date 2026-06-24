package com.back.ovengers.domain.review.dto;

import com.back.ovengers.domain.review.entity.Review;

public record ReviewDetail(
        Long reviewId,
        Integer rating,
        String content
) {
    public static ReviewDetail from(Review review) {
        return new ReviewDetail(
                review.getId(),
                review.getRating(),
                review.getContent()
        );
    }
}
