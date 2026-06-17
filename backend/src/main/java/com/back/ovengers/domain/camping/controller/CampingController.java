package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.CampingDetailResponse;
import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.service.CampingService;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/campings")
@Tag(name = "Camping API", description = "캠핑장 조회 및 검색 API")
public class CampingController {

    private final CampingService campingService;

    @GetMapping
    @Operation(
            summary = "캠핑장 목록 조회",
            description = "캠핑장을 검색하고 목록을 조회할 수 있습니다. (이름/지역 통합 검색 + 페이징)"
    )
    public ResponseEntity<ApiResponse<PageResponse<CampingListResponse>>> getCampList(
            @Parameter(name = "keyword", description = "캠핑장 이름 또는 지역 검색어")
            @RequestParam(required = false)
            String keyword,

            @Parameter(name = "pageable", description = "페이징 정보 (page, size, sort)")
            @PageableDefault(sort = "name") Pageable pageable
    ) {
        Page<CampingListResponse> response = campingService.getCampList(keyword, pageable);

        return ResponseEntity.ok(
                new ApiResponse<>("캠핑장 목록 조회 성공", PageResponse.from(response))
        );
    }

    @GetMapping("/{campingId}")
    @Operation(
            summary = "캠핑장 상세 조회",
            description = "campingId를 통해 캠핑장의 기본 정보, 이미지 목록, 사이트 정보를 조회할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<CampingDetailResponse>> getCampDetail(
            @Parameter(description = "조회할 캠핑장 ID", example = "1")
            @PathVariable Long campingId
    ) {

        CampingDetailResponse response = campingService.getCampDetail(campingId);

        return ResponseEntity.ok(
                new ApiResponse<>("캠핑장 상세 조회 성공", response)
        );
    }
}
