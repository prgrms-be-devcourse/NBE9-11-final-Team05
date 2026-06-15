package com.back.ovengers.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {

    @NotNull(message = "별점은 필수입니다")
    @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "별점은 5점 이하여야 합니다")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수입니다")
    private String content;

    public static ReviewRequest of(Integer rating, String content) {
        ReviewRequest request = new ReviewRequest();
        request.rating = rating;
        request.content = content;
        return request;
    }
}
