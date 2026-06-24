package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.payment.dto.PaymentSummaryResponse;
import com.back.ovengers.domain.reservation.dto.ReservationDetailResponse;
import com.back.ovengers.domain.reservation.dto.ReservationRequest;
import com.back.ovengers.domain.reservation.dto.ReservationResponse;
import com.back.ovengers.domain.reservation.dto.TimeDealReservationRequest;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservation", description = "예약 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성")
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

    @Operation(summary = "예약 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservation(
            @AuthenticationPrincipal User user,
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable @Min(1) Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "예약 상세 조회가 완료되었습니다.",
                        reservationService.getReservation(id, user.getId())
                )
        );
    }

    @Operation(summary = "결제 완료 요약 조회")
    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getSummary(
            @AuthenticationPrincipal User user,
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable @Min(1) Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "결제 완료 조회가 완료되었습니다.",
                        reservationService.getSummary(id, user.getId())
                )
        );
    }

    @Operation(summary = "내 예약 목록 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal User user,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page) {


        return ResponseEntity.ok(
                new ApiResponse<>(
                        "예약 목록 조회가 완료되었습니다.",
                        reservationService.getMyReservations(user.getId(), page)
                )
        );
    }

    /**
     * 타임딜 예약 생성
     *
     * - siteId, checkIn, checkOut, 가격은 서버에서 timeDealId 기반으로 결정한다.
     * - 요청자는 예약자 정보(이름, 연락처, 인원, 요청사항)만 입력한다.
     * - soldCount 증가와 예약 저장이 하나의 트랜잭션으로 처리된다.
     *
     * POST /api/reservations/timedeal/{timeDealId}
     */
    @Operation(summary = "타임딜 예약 생성")
    @PostMapping("/timedeal/{timeDealId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> createTimeDealReservation(
            @AuthenticationPrincipal User user,
            @Parameter(description = "타임딜 ID", example = "1")
            @PathVariable @Min(1) Long timeDealId,
            @RequestBody @Valid TimeDealReservationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "타임딜 예약이 생성되었습니다.",
                        reservationService.createTimeDealReservation(user.getId(), timeDealId, request)
                ));
    }
}
