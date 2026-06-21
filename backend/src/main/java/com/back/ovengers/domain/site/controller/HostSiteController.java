package com.back.ovengers.domain.site.controller;

import com.back.ovengers.domain.site.dto.*;
import com.back.ovengers.domain.site.service.HostSiteService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Host Site", description = "호스트 구역 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host/campings")
@Validated
public class HostSiteController {

    private final HostSiteService hostSiteService;

    @Operation(summary = "내 캠핑장 구역 목록 조회")
    @GetMapping("/{campingId}/sites")
    public ResponseEntity<ApiResponse<List<HostSiteResponse>>> getSites(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(1) Long campingId
    ) {
        List<HostSiteResponse> response =
                hostSiteService.getSites(
                        user.getId(),
                        campingId
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "구역 목록 조회 성공",
                        response
                )
        );
    }

    @Operation(summary = "구역 등록")
    @PostMapping("/{campingId}/sites")
    public ApiResponse<SiteCreateResponse> addSite(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캠핑장 ID", example = "1")
            @PathVariable @Min(1) Long campingId,
            @Valid @RequestBody SiteCreateRequest request
    ) {
        SiteCreateResponse response = hostSiteService.addSite(
                user.getId(),
                campingId,
                request
        );

        return new ApiResponse<>(
                "구역 등록이 완료되었습니다.",
                response
        );
    }

    @Operation(summary = "구역 수정")
    @PatchMapping("/{campingId}/sites/{siteId}")
    public ApiResponse<SiteUpdateResponse> updateSite(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캠핑장 ID", example = "1")
            @PathVariable @Min(1) Long campingId,
            @Parameter(description = "구역 ID", example = "1")
            @PathVariable @Min(1) Long siteId,
            @Valid @RequestBody SiteUpdateRequest request
    ) {
        SiteUpdateResponse response = hostSiteService.updateSite(
                user.getId(),
                campingId,
                siteId,
                request
        );

        return new ApiResponse<>(
                "구역 수정이 완료되었습니다.",
                response
        );
    }

    @Operation(summary = "구역 삭제")
    @DeleteMapping("/{campingId}/sites/{siteId}")
    public ApiResponse<Void> deleteSite(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캠핑장 ID", example = "1")
            @PathVariable @Min(1) Long campingId,
            @Parameter(description = "구역 ID", example = "1")
            @PathVariable @Min(1) Long siteId
    ) {
        hostSiteService.deleteSite(user.getId(), campingId, siteId);

        return new ApiResponse<>("구역 삭제가 완료되었습니다.");
    }
}
