package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.CampingCreateRequest;
import com.back.ovengers.domain.camping.dto.CampingCreateResponse;
import com.back.ovengers.domain.camping.dto.HostCampingListResponse;
import com.back.ovengers.domain.camping.service.HostCampingService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
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
}
