package com.back.ovengers.domain.review.controller;

import com.back.ovengers.domain.review.dto.ReviewRequest;
import com.back.ovengers.domain.review.service.ReviewService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
