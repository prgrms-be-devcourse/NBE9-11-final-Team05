package com.back.ovengers.domain.admin.controller;


import com.back.ovengers.domain.admin.dto.AdminDashboardResponse;
import com.back.ovengers.domain.admin.dto.AdminPendingCampingResponse;
import com.back.ovengers.domain.admin.dto.CampingBulkApproveRequest;
import com.back.ovengers.domain.admin.dto.CampingRejectRequest;
import com.back.ovengers.domain.admin.service.AdminService;
import com.back.ovengers.domain.camping.external.GoCampingSyncService;
import com.back.ovengers.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final GoCampingSyncService goCampingSyncService;
    private final AdminService adminService;

    @Operation(summary = "초기 데이터 동기화", description = "고캠핑 API 데이터 초기화")
    @GetMapping("/init-data")
    public void init() {
        goCampingSyncService.syncInitialData();
    }


    @Operation(summary = "초기 이미지 데이터 동기화", description = "고캠핑 API 이미지 데이터 초기화")
    @GetMapping("/init-images-data")
    public void initImages() {
        goCampingSyncService.syncImageData();
    }

    @Operation(summary = "관리자 대시보드 조회")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse response = adminService.getDashboard();
        return ResponseEntity.ok(new ApiResponse<>(
                "관리자 대시보드 조회 성공",
                response
        ));
    }

    // 다른 상태의 캠핑장을 보고싶으면 스테이터스 추가
    @Operation(summary = "캠핑장 승인 대기 목록 조회")
    @GetMapping("/campings")
    public ResponseEntity<ApiResponse<AdminPendingCampingResponse>> getPendingCampingList(
            //@RequestParam CampingStatus status,
            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    ) {
        AdminPendingCampingResponse response = adminService.getPendingCampingList(pageable);
        return ResponseEntity.ok(new ApiResponse<>(
                "캠핑장 승인 대기 목록 조회 성공",
                response
        ));
    }

    // 캠핑장 승인
    @Operation(summary = "캠핑장 승인")
    @PatchMapping("/campings/{campingId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveCamping(
            @Parameter(description = "캠핑장 ID", example = "1")
            @PathVariable @Min(1) Long campingId
    ) {
        adminService.approveCamping(campingId);
        return ResponseEntity.ok(new ApiResponse<>("캠핑장 승인 성공"));
    }

    // 캠핑장 거절
    @Operation(summary = "캠핑장 거절")
    @PatchMapping("/campings/{campingId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectCamping(
            @Parameter(description = "캠핑장 ID", example = "1")
            @PathVariable @Min(1) Long campingId,
            @Valid @RequestBody CampingRejectRequest request
    ) {
        adminService.rejectCamping(campingId, request);
        return ResponseEntity.ok(new ApiResponse<>("캠핑장 거절 성공"));
    }

    // 캠핑장 일괄 승인
    @Operation(summary = "캠핑장 일괄 승인")
    @PatchMapping("/campings/approve")
    public ResponseEntity<ApiResponse<Void>> approveCampingList(
            @Valid @RequestBody CampingBulkApproveRequest request
    ) {
        adminService.approveCampingList(request);
        return ResponseEntity.ok(new ApiResponse<>("캠핑장 일괄 승인 성공"));
    }
}
