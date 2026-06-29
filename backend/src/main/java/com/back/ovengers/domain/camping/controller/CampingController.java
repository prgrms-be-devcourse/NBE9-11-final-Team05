package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.CampingDetailResponse;
import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.dto.CampingSearchResponse;
import com.back.ovengers.domain.camping.service.CampingService;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @Operation(summary = "캠핑장 검색", description = "날짜, 인원, 가격 등 조건으로 예약 가능한 캠핑장을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CampingSearchResponse>>> searchCampings(
            @Parameter(description = "캠핑장 이름 또는 도시 검색어")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "체크인 날짜 (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate checkIn,
            @Parameter(description = "체크아웃 날짜 (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate checkOut,
            @Parameter(description = "숙박 인원")
            @RequestParam(required = false) Integer guestCount,
            @Parameter(description = "객실 수")
            @RequestParam(required = false) Integer roomCount,
            @Parameter(description = "최소 가격 (1박 기준)")
            @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "최대 가격 (1박 기준)")
            @RequestParam(required = false) Integer maxPrice,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<CampingSearchResponse> response = campingService.searchAvailableCampings(
                keyword, checkIn, checkOut, guestCount, roomCount, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(new ApiResponse<>("캠핑장 검색 성공", PageResponse.from(response)));
    }
}
