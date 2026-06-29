package com.back.ovengers.domain.payment.client;

import com.back.ovengers.domain.payment.dto.TossConfirmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 부하테스트 전용 TossPaymentClient stub.
 *
 * loadtest 프로파일에서만 활성화되며, 실제 토스 API를 호출하지 않고
 * 즉시 성공 응답을 반환한다. 덕분에:
 *  - 토스 rate limit에 걸리지 않음
 *  - 실제 결제가 발생하지 않음
 *  - 네트워크 레이턴시 없이 서버 내부 동시성 로직만 순수하게 측정 가능
 */
@Slf4j
@Component
@Profile("loadtest")
public class StubTossPaymentClient implements TossPaymentClient {

    @Override
    public TossConfirmResponse confirm(String paymentKey, String orderId, Integer amount) {
        log.debug("[STUB] 토스 confirm 호출 - paymentKey={}, orderId={}", paymentKey, orderId);
        // TossConfirmResponse 필드에 맞춰 조정 필요 (아래는 예시)
        return new TossConfirmResponse(
                paymentKey,
                orderId,
                "카드",       // method
                amount,
                java.time.OffsetDateTime.now().toString(),  // approvedAt
                "DONE"
        );
    }

    @Override
    public void cancel(String paymentKey, String cancelReason) {
        log.debug("[STUB] 토스 cancel 호출 - paymentKey={}", paymentKey);
        // 아무것도 하지 않음 (성공으로 간주)
    }
}
