package com.back.ovengers.domain.timedeal.controller;

import com.back.ovengers.domain.timedeal.dto.TimeDealCreateRequest;
import com.back.ovengers.domain.timedeal.dto.TimeDealResponse;
import com.back.ovengers.domain.timedeal.dto.TimeDealUpdateRequest;
import com.back.ovengers.domain.timedeal.service.TimeDealService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timedeals")
@RequiredArgsConstructor
public class TimeDealController {

    private final TimeDealService timeDealService;

    @PostMapping("/host")
    public ResponseEntity<ApiResponse<TimeDealResponse>> create(
            Authentication authentication,
            @Valid @RequestBody TimeDealCreateRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("타임딜이 등록되었습니다.",
                        timeDealService.createTimeDeal(user.getId(), request)));
    }

    @PatchMapping("/host/{timeDealId}")
    public ResponseEntity<ApiResponse<TimeDealResponse>> update(
            Authentication authentication,
            @PathVariable Long timeDealId,
            @Valid @RequestBody TimeDealUpdateRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                new ApiResponse<>("타임딜이 수정되었습니다.",
                        timeDealService.updateTimeDeal(user.getId(), timeDealId, request)));
    }

    @PatchMapping("/host/{timeDealId}/cancel")
    public ResponseEntity<Void> cancel(
            Authentication authentication,
            @PathVariable Long timeDealId
    ) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        timeDealService.cancelTimeDeal(userId, timeDealId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/host/{timeDealId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable Long timeDealId
    ) {
        User user = (User) authentication.getPrincipal();
        timeDealService.deleteTimeDeal(user.getId(), timeDealId);
        return ResponseEntity.ok(new ApiResponse<>("타임딜이 삭제되었습니다."));
    }

    @GetMapping("/host/my")
    public ResponseEntity<ApiResponse<Page<TimeDealResponse>>> getMyTimeDeals(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                new ApiResponse<>("호스트 타임딜 목록 조회 성공",
                        timeDealService.getMyTimeDeals(user.getId(), pageable)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TimeDealResponse>>> getActiveTimeDeals(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("타임딜 목록 조회 성공",
                        timeDealService.getActiveTimeDeals(pageable)));
    }

    @GetMapping("/{timeDealId}")
    public ResponseEntity<ApiResponse<TimeDealResponse>> getTimeDeal(
            @PathVariable Long timeDealId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("타임딜 단건 조회 성공",
                        timeDealService.getTimeDeal(timeDealId)));
    }
}
