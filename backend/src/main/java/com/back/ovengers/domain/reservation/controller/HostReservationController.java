package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.reservation.dto.HostReservationResponse;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/host/reservations")
@RequiredArgsConstructor
public class HostReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<PageResponse<HostReservationResponse>>> getReservations(
            @AuthenticationPrincipal User user,
            @PageableDefault Pageable pageable
    ) {
        Page<HostReservationResponse> response = reservationService.getHostReservations(user.getId(), pageable);


        return ResponseEntity.ok(
                    new ApiResponse<>(
                            "호스트 캠핑장 목록 조회 성공",
                            PageResponse.from(response)
                    )
        );
    }

}
