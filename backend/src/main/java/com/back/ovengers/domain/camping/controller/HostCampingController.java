package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.service.HostCampingService;
import com.back.ovengers.domain.site.dto.SiteCreateRequest;
import com.back.ovengers.domain.site.dto.SiteCreateResponse;
import com.back.ovengers.domain.site.dto.SiteUpdateRequest;
import com.back.ovengers.domain.site.dto.SiteUpdateResponse;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host/campings")
public class HostCampingController {

    private final HostCampingService hostCampingService;

    @PostMapping
    public ResponseEntity<ApiResponse<CampingCreateResponse>> register(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CampingCreateRequest request
    ) {

        CampingCreateResponse response =
                hostCampingService.register(user.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "캠핑장 등록 신청이 완료되었습니다.",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HostCampingListResponse>>> getMyCampings(
            @AuthenticationPrincipal User user
    ) {
        List<HostCampingListResponse> response =
                hostCampingService.getMyCampings(user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "내 캠핑장 목록 조회 성공",
                        response
                )
        );
    }

    @PatchMapping("/{campingId}")
    public ResponseEntity<ApiResponse<CampingUpdateResponse>> updateCamping(
            @AuthenticationPrincipal User user,
            @PathVariable Long campingId,
            @RequestBody CampingUpdateRequest request
    ) {
        CampingUpdateResponse response =
                hostCampingService.updateCamping(user.getId(), campingId, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "캠핑장 정보 수정이 완료되었습니다.",
                        response
                )
        );
    }

    @DeleteMapping("/{campingId}")
    public ResponseEntity<ApiResponse<Void>> deleteCamping(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(1) Long campingId
    ) {
        hostCampingService.deleteCamping(user.getId(), campingId);

        return ResponseEntity.ok(
                new ApiResponse<>("캠핑장이 삭제되었습니다.")
        );
    }

    @PostMapping("/{campingId}/sites")
    public ApiResponse<SiteCreateResponse> addSite(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(1) Long campingId,
            @Valid @RequestBody SiteCreateRequest request
    ) {
        SiteCreateResponse response = hostCampingService.addSite(
                user.getId(),
                campingId,
                request
        );

        return new ApiResponse<>(
                "구역 등록이 완료되었습니다.",
                response
        );
    }

    @PatchMapping("/{campingId}/sites/{siteId}")
    public ApiResponse<SiteUpdateResponse> updateSite(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(1) Long siteId,
            @Valid @RequestBody SiteUpdateRequest request
    ) {
        SiteUpdateResponse response = hostCampingService.updateSite(
                user.getId(),
                siteId,
                request
        );

        return new ApiResponse<>(
                "구역 수정이 완료되었습니다.",
                response
        );
    }

    @DeleteMapping("/{campingId}/sites/{siteId}")
    public ApiResponse<Void> deleteSite(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(1) Long siteId
    ) {
        hostCampingService.deleteSite(user.getId(), siteId);

        return new ApiResponse<>("구역 삭제가 완료되었습니다.");
    }

    @PostMapping("/{campingId}/images")
    public ApiResponse<CampingImageCreateResponse> addCampingImage(
            @AuthenticationPrincipal User user,
            @PathVariable Long campingId,
            @Valid @RequestBody CampingImageCreateRequest request
    ) {
        CampingImageCreateResponse response =
                hostCampingService.addCampingImage(user.getId(), campingId, request);

        return new ApiResponse<>("캠핑장 이미지가 등록되었습니다.", response);
    }

    @DeleteMapping("/{campingId}/images/{imageId}")
    public ApiResponse<Void> deleteCampingImage(
            @AuthenticationPrincipal User user,
            @PathVariable Long campingId,
            @PathVariable Long imageId
    ) {
        hostCampingService.deleteCampingImage(user.getId(), campingId, imageId);

        return new ApiResponse<>("캠핑장 이미지가 삭제되었습니다.");
    }
}
