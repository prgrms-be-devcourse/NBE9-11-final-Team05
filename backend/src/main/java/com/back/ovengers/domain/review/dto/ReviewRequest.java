package com.back.ovengers.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull(message = "별점은 필수입니다")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다")
        Integer rating,

        @NotBlank(message = "리뷰 내용은 필수입니다")
        String content
) {
    public static ReviewRequest of(Integer rating, String content) {
        return new ReviewRequest(rating, content);
    }
}
