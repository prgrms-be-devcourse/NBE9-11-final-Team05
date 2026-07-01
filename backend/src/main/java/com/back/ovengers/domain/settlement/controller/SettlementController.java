package com.back.ovengers.domain.settlement.controller;

import com.back.ovengers.domain.settlement.dto.SettlementDetailWithListResponse;
import com.back.ovengers.domain.settlement.dto.SettlementListResponse;
import com.back.ovengers.domain.settlement.service.SettlementService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host/settlements")
@Tag(name = "Settlement", description = "정산 관련 API")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    @Operation(summary = "정산 목록 조회", description = "호스트가 자신의 정산 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<SettlementListResponse>>> getMySettlements(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "정산 목록 조회가 완료되었습니다.",
                        settlementService.getMySettlements(user.getId(), page)
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "정산 상세 조회", description = "호스트가 자신의 정산 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<SettlementDetailWithListResponse>> getSettlementDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "정산 상세 조회가 완료되었습니다.",
                        settlementService.getSettlementDetail(id, user.getId())
                )
        );
    }
}
