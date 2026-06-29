package com.back.ovengers.domain.payment.client;

import com.back.ovengers.domain.payment.dto.TossConfirmResponse;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@Profile("!loadtest")
@RequiredArgsConstructor
public class TossPaymentClientImpl implements TossPaymentClient {

    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String secretKey;

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL  = "https://api.tosspayments.com/v1/payments/%s/cancel";

    @Override
    public TossConfirmResponse confirm(String paymentKey, String orderId, Integer amount) {
        HttpHeaders headers = buildHeaders();
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
        try {
            ResponseEntity<TossConfirmResponse> response = restTemplate.exchange(
                    TOSS_CONFIRM_URL, HttpMethod.POST,
                    new HttpEntity<>(body, headers), TossConfirmResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOSS_CONFIRM_FAIL);
        }
    }

    @Override
    public void cancel(String paymentKey, String cancelReason) {
        HttpHeaders headers = buildHeaders();
        Map<String, Object> body = Map.of("cancelReason", cancelReason);

        // 기존: 예외를 삼키고 로그만 남김 → 호출부가 실패를 알 수 없었음
        // 변경: 예외를 그대로 던져서 호출부(PaymentScheduler 등)가 실패를 판단하게 함
        restTemplate.exchange(
                String.format(TOSS_CANCEL_URL, paymentKey),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Void.class
        );
        // 예외 발생 시 그대로 전파 → 스케줄러가 markCancelFailed() 호출
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
