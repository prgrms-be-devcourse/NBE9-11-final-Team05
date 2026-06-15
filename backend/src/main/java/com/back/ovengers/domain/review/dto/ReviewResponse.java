package com.back.ovengers.domain.review.dto;

import com.back.ovengers.domain.review.entity.Review;
import com.back.ovengers.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {

    private Long reviewId;
    private String writerNickname;
    private String writerImageUrl;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    private ReviewResponse(Review review) {
        User user = review.getUser();
        this.reviewId = review.getId();
        this.writerNickname = user.getDeletedAt() != null
                ? "탈퇴한 사용자"
                : user.getNickname();
        this.writerImageUrl = user.getDeletedAt() != null
                ? null
                : user.getImageUrl();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }

    public static ReviewResponse from(Review review){
        return new ReviewResponse(review);
    }
}