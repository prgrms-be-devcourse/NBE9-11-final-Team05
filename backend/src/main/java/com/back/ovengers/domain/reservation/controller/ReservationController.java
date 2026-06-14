package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.reservation.dto.ReservationRequest;
import com.back.ovengers.domain.reservation.dto.ReservationResponse;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @RequestBody @Valid ReservationRequest request) {

        // TODO: jwt filter 구현 후 userDetails.getId() 로 교체
        Long userId = 2L;

        ReservationResponse response = reservationService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "예약이 생성되었습니다.",
                                response
                        )
                );
    }
}
