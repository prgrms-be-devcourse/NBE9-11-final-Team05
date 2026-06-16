package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.service.CampingService;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/campings")
public class CampingController {

    private final CampingService campingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CampingListResponse>>> getCamps(
            @RequestParam(required = false) String keyword,
            @PageableDefault Pageable pageable
    ) {
        Page<CampingListResponse> camps = campingService.getCamps(keyword, pageable);

        return ResponseEntity.ok(
                new ApiResponse<>("캠핑장 목록 조회 성공", PageResponse.from(camps))
        );
    }

}
