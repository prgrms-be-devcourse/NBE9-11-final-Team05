package com.back.ovengers.domain.review.dto;

import com.back.ovengers.domain.review.entity.Review;
import lombok.Getter;

@Getter
public class ReviewDetail {
    private Long reviewId;
    private Integer rating;
    private String content;

    private ReviewDetail(Review review){
        this.reviewId = review.getId();
        this.rating = review.getRating();
        this.content = review.getContent();
    }

    public static ReviewDetail from(Review review) {
        return new ReviewDetail(review);
    }
}
