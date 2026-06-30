package com.back.ovengers.domain.payment.client;

import com.back.ovengers.domain.payment.dto.TossConfirmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 부하테스트 전용 TossPaymentClient stub.
 *
 * 인위적 지연(300ms)을 추가해 실제 토스 API의 네트워크 레이턴시를 흉내낸다.
 * 이 지연이 있어야 "락 안에서 호출 vs 락 밖에서 호출"의 성능 차이가
 * k6 부하테스트에서 측정 가능해진다.
 * (지연이 0ms면 락 홀딩 시간 차이가 거의 없어 개선 효과가 안 보임)
 */
@Slf4j
@Component
@Profile("loadtest")
public class StubTossPaymentClient implements TossPaymentClient {

    // 토스 평균 응답시간을 가정한 인위적 지연
    private static final int SIMULATED_LATENCY_MS = 300;

    @Override
    public TossConfirmResponse confirm(String paymentKey, String orderId, Integer amount) {
        simulateNetworkLatency();
        log.debug("[STUB] 토스 confirm 호출 - paymentKey={}, orderId={}", paymentKey, orderId);

        return new TossConfirmResponse(
                paymentKey,
                orderId,
                "카드",
                amount,
                java.time.OffsetDateTime.now().toString(),
                "DONE"
        );
    }

    @Override
    public void cancel(String paymentKey, String cancelReason) {
        simulateNetworkLatency();
        log.debug("[STUB] 토스 cancel 호출 - paymentKey={}", paymentKey);
    }

    private void simulateNetworkLatency() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
