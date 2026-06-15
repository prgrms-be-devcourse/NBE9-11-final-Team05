package com.back.ovengers.domain.review.controller;

import com.back.ovengers.domain.review.dto.ReviewRequest;
import com.back.ovengers.domain.review.dto.ReviewResponse;
import com.back.ovengers.domain.review.service.ReviewService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
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
}
