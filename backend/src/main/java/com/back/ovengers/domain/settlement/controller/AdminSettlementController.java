package com.back.ovengers.domain.settlement.controller;

import com.back.ovengers.domain.settlement.dto.AdminSettlementListResponse;
import com.back.ovengers.domain.settlement.dto.SettlementCompleteResponse;
import com.back.ovengers.domain.settlement.dto.SettlementGenerateRequest;
import com.back.ovengers.domain.settlement.dto.SettlementGenerateResponse;
import com.back.ovengers.domain.settlement.service.SettlementService;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.back.ovengers.domain.user.entity.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlements")
public class AdminSettlementController {

    private final SettlementService settlementService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SettlementGenerateResponse>> generate(
            @RequestBody SettlementGenerateRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "정산이 생성되었습니다.",
                        settlementService.generate(request.settlementDate())
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminSettlementListResponse>>> getAllSettlements(
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "정산 목록 조회가 완료되었습니다.",
                        settlementService.getAllSettlements(page)
                )
        );
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<SettlementCompleteResponse>> completeSettlement(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "정산이 완료 처리되었습니다.",
                        settlementService.completeSettlement(id)
                )
        );
    }
}