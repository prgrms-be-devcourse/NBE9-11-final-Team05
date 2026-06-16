package com.back.ovengers.domain.payment.controller;

import com.back.ovengers.domain.payment.dto.PaymentRequest;
import com.back.ovengers.domain.payment.dto.PaymentResponse;
import com.back.ovengers.domain.payment.service.PaymentService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

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
}
