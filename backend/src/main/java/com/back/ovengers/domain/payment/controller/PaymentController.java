package com.back.ovengers.domain.payment.controller;

import com.back.ovengers.domain.payment.dto.PaymentConfirmRequest;
import com.back.ovengers.domain.payment.dto.PaymentConfirmResponse;
import com.back.ovengers.domain.payment.dto.PaymentRequest;
import com.back.ovengers.domain.payment.dto.PaymentResponse;
import com.back.ovengers.domain.payment.service.PaymentService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 생성", description = "예약에 대한 결제를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid PaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "결제가 생성되었습니다.",
                        paymentService.create(user.getId(), request)
                ));
    }

    @Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 처리합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirm(
            @RequestBody @Valid PaymentConfirmRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "결제가 완료되었습니다.",
                        paymentService.confirm(request)
                )
        );
    }
}
