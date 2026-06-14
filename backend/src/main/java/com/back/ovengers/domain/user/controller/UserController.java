package com.back.ovengers.domain.user.controller;

import com.back.ovengers.domain.reservation.dto.ReservationResponse;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserController {

    private final ReservationService reservationService;

    // 내 예약 목록 조회
    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @RequestParam(defaultValue = "0") int page) {

        Long userId = 2L;

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "예약 목록 조회가 완료되었습니다.",
                        reservationService.getMyReservations(userId, page)
                )
        );
    }
}
