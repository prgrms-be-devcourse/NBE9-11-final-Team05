package com.back.ovengers.domain.payment.controller;

import com.back.ovengers.domain.payment.dto.PaymentConfirmRequest;
import com.back.ovengers.domain.payment.dto.PaymentResponse;
import com.back.ovengers.domain.payment.service.PaymentConfirmServiceBefore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * [측정 전용 - 임시]
 *
 * confirm 3단계 분리의 성능 개선 효과를 Before/After로 비교 측정하기 위한 컨트롤러.
 *
 * After 쪽은 별도 엔드포인트가 필요 없다 — 지금 운영 중인
 * PaymentService.confirm() (= /api/payments/confirm) 이 이미 3단계 분리
 * 구조이므로 그대로 사용하면 됨.
 *
 * 여기서는 "Before(개선 전 구조)"를 재현한 confirm-before 만 추가한다.
 *
 * loadtest 프로파일에서만 활성화. 측정이 끝나면 삭제할 것.
 */
@RestController
@Profile("loadtest")
@RequiredArgsConstructor
public class PaymentConfirmBenchmarkController {

    private final PaymentConfirmServiceBefore paymentConfirmServiceBefore;

    @PostMapping("/api/test/confirm-before")
    public PaymentResponse confirmBefore(@RequestBody PaymentConfirmRequest request) {
        return paymentConfirmServiceBefore.confirm(request);
    }
}
