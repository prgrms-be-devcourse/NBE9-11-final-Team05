package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.CampingDetailResponse;
import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.dto.CampingSearchResponse;
import com.back.ovengers.domain.camping.service.CampingService;
import com.back.ovengers.domain.site.dto.SiteResponse;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/campings")
@Tag(name = "Camping API", description = "캠핑장 조회 및 검색 API")
@Validated
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

    @GetMapping("/{campingId}/available-sites")
    @Operation(
            summary = "캠핑장 이용 가능한 사이트 조회",
            description = """
                선택한 체크인/체크아웃 날짜 기준으로 예약 가능한 사이트 목록을 조회합니다.
                
                - 예약 상태가 PENDING / CONFIRMED 인 예약 기준으로 제외됩니다.
                - 사이트의 총 수량 대비 예약 수량을 계산하여 남은 수량이 있는 사이트만 반환됩니다.
                """
    )
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getAvailableSites(
            @PathVariable @Min(1) Long campingId,
            @Parameter(description = "체크인 날짜 (yyyy-MM-dd)", required = true)
            @RequestParam LocalDate checkIn,
            @Parameter(description = "체크아웃 날짜 (yyyy-MM-dd)", required = true)
            @RequestParam LocalDate checkOut
    ) {

        List<SiteResponse> response = campingService.getAvailableSites(campingId, checkIn, checkOut);

        return ResponseEntity.ok(
                new ApiResponse<>("이용 가능한 구역 조회 성공", response)
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
