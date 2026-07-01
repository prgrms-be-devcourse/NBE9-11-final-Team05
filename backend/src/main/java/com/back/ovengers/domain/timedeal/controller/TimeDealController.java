package com.back.ovengers.domain.timedeal.controller;

import com.back.ovengers.domain.timedeal.dto.TimeDealCreateRequest;
import com.back.ovengers.domain.timedeal.dto.TimeDealResponse;
import com.back.ovengers.domain.timedeal.dto.TimeDealUpdateRequest;
import com.back.ovengers.domain.timedeal.service.TimeDealService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 1. Import 추가됨
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timedeals")
@RequiredArgsConstructor
@Tag(name = "TimeDeal", description = "타임딜 관련 API")
public class TimeDealController {

    private final TimeDealService timeDealService;

    @PostMapping("/host")
    @Operation(summary = "타임딜 등록", description = "호스트가 새로운 타임딜을 등록합니다.")
    public ResponseEntity<ApiResponse<TimeDealResponse>> create(
            @AuthenticationPrincipal User user, // 2. @AuthenticationPrincipal 적용
            @Valid @RequestBody TimeDealCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("타임딜이 등록되었습니다.",
                        timeDealService.createTimeDeal(user.getId(), request)));
    }

    @PatchMapping("/host/{timeDealId}")
    @Operation(summary = "타임딜 수정", description = "호스트가 자신의 타임딜을 수정합니다.")
    public ResponseEntity<ApiResponse<TimeDealResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long timeDealId,
            @Valid @RequestBody TimeDealUpdateRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("타임딜이 수정되었습니다.",
                        timeDealService.updateTimeDeal(user.getId(), timeDealId, request)));
    }

    @PatchMapping("/host/{timeDealId}/cancel")
    @Operation(summary = "타임딜 취소", description = "호스트가 자신의 타임딜을 취소합니다.")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal User user,
            @PathVariable Long timeDealId
    ) {
        timeDealService.cancelTimeDeal(user.getId(), timeDealId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/host/{timeDealId}")
    @Operation(summary = "타임딜 삭제", description = "호스트가 자신의 타임딜을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long timeDealId
    ) {
        timeDealService.deleteTimeDeal(user.getId(), timeDealId);
        return ResponseEntity.ok(new ApiResponse<>("타임딜이 삭제되었습니다."));
    }

    @GetMapping("/host/my")
    @Operation(summary = "내 타임딜 조회", description = "호스트가 자신의 타임딜 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<TimeDealResponse>>> getMyTimeDeals(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("호스트 타임딜 목록 조회 성공",
                        timeDealService.getMyTimeDeals(user.getId(), pageable)));
    }

    @GetMapping
    @Operation(summary = "타임딜 목록 조회", description = "타임딜 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<TimeDealResponse>>> getActiveTimeDeals(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("타임딜 목록 조회 성공",
                        timeDealService.getActiveTimeDeals(pageable)));
    }

    @GetMapping("/{timeDealId}")
    @Operation(summary = "타임딜 단건 조회", description = "특정 타임딜의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TimeDealResponse>> getTimeDeal(
            @PathVariable Long timeDealId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("타임딜 단건 조회 성공",
                        timeDealService.getTimeDeal(timeDealId)));
    }
}
