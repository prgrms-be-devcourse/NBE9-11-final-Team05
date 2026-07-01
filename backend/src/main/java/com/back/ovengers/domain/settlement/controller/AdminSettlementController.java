package com.back.ovengers.domain.settlement.controller;

import com.back.ovengers.domain.settlement.dto.AdminSettlementListResponse;
import com.back.ovengers.domain.settlement.dto.SettlementCompleteResponse;
import com.back.ovengers.domain.settlement.dto.SettlementGenerateRequest;
import com.back.ovengers.domain.settlement.dto.SettlementGenerateResponse;
import com.back.ovengers.domain.settlement.service.SettlementService;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlements")
@Tag(name = "Admin Settlement", description = "관리자 정산 관련 API")
public class AdminSettlementController {

    private final SettlementService settlementService;

    @PostMapping("/generate")
    @Operation(summary = "정산 생성", description = "새로운 정산을 생성합니다.")
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
    @Operation(summary = "정산 목록 조회", description = "정산 목록을 조회합니다. (페이징 처리)")
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
    @Operation(summary = "정산 완료 처리", description = "특정 정산을 완료 상태로 변경합니다.")
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
