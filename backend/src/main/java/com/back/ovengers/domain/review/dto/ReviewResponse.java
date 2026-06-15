package com.back.ovengers.domain.review.dto;

import com.back.ovengers.domain.review.entity.Review;
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
        this.reviewId = review.getId();
        this.writerNickname = review.getUser().getNickname();
        this.writerImageUrl = review.getUser().getImageUrl();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }

    public static ReviewResponse from(Review review){
        return new ReviewResponse(review);
    }
}