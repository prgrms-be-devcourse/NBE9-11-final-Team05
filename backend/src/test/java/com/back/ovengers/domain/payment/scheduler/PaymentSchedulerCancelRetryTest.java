package com.back.ovengers.domain.payment.scheduler;

import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSchedulerCancelRetryTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private TimeDealRepository timeDealRepository;
    @Mock private TossPaymentClient tossPaymentClient;

    @InjectMocks
    private PaymentScheduler paymentScheduler;

    private Payment makeFailedPayment(int retryCount) {
        return Payment.builder()
                .reservation(mock(Reservation.class))
                .orderId("ORDER-001")
                .paymentKey("test-key-001")
                .paidPrice(100000)
                .status(PaymentStatus.CANCELLED)
                .cancelFailedAt(LocalDateTime.now().minusMinutes(10))
                .cancelRetryCount(retryCount)
                .build();
    }

    // setUp 불필요 — retryCancelFailedPayments()는 expirePendingPayments()와
    // 독립된 메서드라 findByStatusAndCreatedAtBefore / findAbandonedReservations stubbing 불필요

    @Test
    @DisplayName("재시도 성공 → cancelFailedAt 초기화, retryCount 0으로 리셋")
    void retry_success_clearsCancelFailed() {
        // given
        Payment payment = makeFailedPayment(1);
        given(paymentRepository.findByCancelFailedAtIsNotNullAndCancelRetryCountLessThan(3))
                .willReturn(List.of(payment));
        willDoNothing().given(tossPaymentClient).cancel(anyString(), anyString());

        // when
        paymentScheduler.retryCancelFailedPayments();

        // then
        assertThat(payment.getCancelFailedAt()).isNull();
        assertThat(payment.getCancelRetryCount()).isEqualTo(0);
        then(tossPaymentClient).should(times(1)).cancel(eq("test-key-001"), anyString());
    }

    @Test
    @DisplayName("재시도 실패 → retryCount 1 증가, cancelFailedAt 갱신")
    void retry_fail_incrementsRetryCount() {
        // given
        Payment payment = makeFailedPayment(1);
        given(paymentRepository.findByCancelFailedAtIsNotNullAndCancelRetryCountLessThan(3))
                .willReturn(List.of(payment));
        willThrow(new RuntimeException("토스 서버 오류"))
                .given(tossPaymentClient).cancel(anyString(), anyString());

        // when
        paymentScheduler.retryCancelFailedPayments();

        // then
        assertThat(payment.getCancelRetryCount()).isEqualTo(2);  // 1 → 2
        assertThat(payment.getCancelFailedAt()).isNotNull();
    }

    @Test
    @DisplayName("재시도 3회 초과 → 토스 호출 안 함 (수동 처리 대상)")
    void retry_exhausted_skipsCall() {
        // given — retryCount가 이미 3이면 쿼리 조건(< 3)에 안 걸려야 하지만,
        // isCancelRetryExhausted() 내부 방어 로직도 검증
        Payment payment = makeFailedPayment(3);
        given(paymentRepository.findByCancelFailedAtIsNotNullAndCancelRetryCountLessThan(3))
                .willReturn(List.of(payment));  // 혹시 경계값으로 들어온 경우

        // when
        paymentScheduler.retryCancelFailedPayments();

        // then — 토스 호출이 일어나지 않아야 함
        then(tossPaymentClient).should(never()).cancel(anyString(), anyString());
    }

    @Test
    @DisplayName("실패 건 없으면 토스 호출 안 함")
    void noFailedPayments_noCall() {
        // given
        given(paymentRepository.findByCancelFailedAtIsNotNullAndCancelRetryCountLessThan(3))
                .willReturn(List.of());

        // when
        paymentScheduler.retryCancelFailedPayments();

        // then
        then(tossPaymentClient).should(never()).cancel(anyString(), anyString());
    }

    @Test
    @DisplayName("재시도 성공 건과 실패 건 혼재 → 각각 독립 처리")
    void mixed_successAndFail_handledIndependently() {
        // given
        Payment successPayment = makeFailedPayment(1);
        Payment failPayment    = makeFailedPayment(2);

        given(paymentRepository.findByCancelFailedAtIsNotNullAndCancelRetryCountLessThan(3))
                .willReturn(List.of(successPayment, failPayment));

        // 첫 번째 호출 성공, 두 번째 호출 실패
        willDoNothing()
                .willThrow(new RuntimeException("토스 오류"))
                .given(tossPaymentClient).cancel(anyString(), anyString());

        // when
        paymentScheduler.retryCancelFailedPayments();

        // then
        // 성공한 건 — 초기화
        assertThat(successPayment.getCancelFailedAt()).isNull();
        assertThat(successPayment.getCancelRetryCount()).isEqualTo(0);

        // 실패한 건 — retryCount 증가
        assertThat(failPayment.getCancelRetryCount()).isEqualTo(3);
    }
}
