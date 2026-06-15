package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.reservation.dto.ReservationDetailResponse;
import com.back.ovengers.domain.reservation.dto.ReservationRequest;
import com.back.ovengers.domain.reservation.dto.ReservationResponse;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ReservationRequest request) {


        ReservationResponse response = reservationService.create(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "예약이 생성되었습니다.",
                                response
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservation(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {


        return ResponseEntity.ok(
                new ApiResponse<>(
                        "예약 상세 조회가 완료되었습니다.",
                        reservationService.getReservation(id, user.getId())
                )
        );
    }
}
