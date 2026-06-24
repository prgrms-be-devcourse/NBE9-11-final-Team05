package com.back.ovengers.domain.review.dto;

import com.back.ovengers.domain.review.entity.Review;
import com.back.ovengers.domain.user.entity.User;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        String writerNickname,
        String writerImageUrl,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        User user = review.getUser();
        boolean isDeleted = user == null || user.getDeletedAt() != null;
        return new ReviewResponse(
                review.getId(),
                isDeleted ? "탈퇴한 사용자" : user.getNickname(),
                isDeleted ? null : user.getImageUrl(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
