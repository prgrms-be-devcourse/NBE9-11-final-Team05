package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.CampingCreateRequest;
import com.back.ovengers.domain.camping.dto.CampingCreateResponse;
import com.back.ovengers.domain.camping.dto.HostCampingListResponse;
import com.back.ovengers.domain.camping.service.HostCampingService;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host/campings")
public class HostCampingController {

    private final HostCampingService hostCampingService;

    @PostMapping
    public ResponseEntity<ApiResponse<CampingCreateResponse>> register(
            @Valid @RequestBody CampingCreateRequest request
    ) {
        // TODO: JWT 필터 구현 후 @AuthenticationPrincipal로 로그인 사용자 ID 추출
        Long tempHostId = 1L;

        CampingCreateResponse response =
                hostCampingService.register(tempHostId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "캠핑장 등록 신청이 완료되었습니다.",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HostCampingListResponse>>> getMyCampings(
            @RequestParam Long hostId
    ) {
        List<HostCampingListResponse> response =
                hostCampingService.getMyCampings(hostId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "내 캠핑장 목록 조회 성공",
                        response
                )
        );
    }
}
