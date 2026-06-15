package com.back.ovengers.domain.review.controller;

import com.back.ovengers.domain.review.dto.ReviewRequest;
import com.back.ovengers.domain.review.dto.ReviewResponse;
import com.back.ovengers.domain.review.service.ReviewService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createReview(
            @AuthenticationPrincipal User user,
            @RequestParam Long reservationId,
            @Valid @RequestBody ReviewRequest request
    ){
        reviewService.createReview(user, reservationId, request);
        return ResponseEntity.ok(new ApiResponse<>("리뷰가 작성되었습니다."));
    }

    // 최신순 10개씩
    @GetMapping("/campings/{campingId}/reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getCampingReviews(
            @PathVariable Long campingId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReviewResponse> reviews = reviewService.getCampingReviews(campingId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(
                "캠핑장 리뷰 목록 조회 성공",
                PageResponse.from(reviews)
        ));
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request
    ) {
        reviewService.updateReview(user, reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>("리뷰가 수정되었습니다."));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(value = 1, message = "리뷰 ID가 올바르지 않습니다.") Long reviewId
    ) {
        reviewService.deleteReview(user, reviewId);
        return ResponseEntity.ok(new ApiResponse<>("리뷰가 삭제되었습니다."));
    }
}
