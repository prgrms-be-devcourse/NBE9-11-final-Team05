package com.back.ovengers.domain.payment.scheduler;

import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.timedeal.entity.TimeDealStatus;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final TimeDealRepository timeDealRepository;
    private final ReservationRepository reservationRepository;
    private final TossPaymentClient tossPaymentClient;

    private static final int EXPIRE_MINUTES = 15;
    private static final int MAX_CANCEL_RETRY = 3;  // 최대 재시도 횟수

    // ── 기존 로직: 미결제 만료 처리 ──────────────────────────────────────────

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirePendingPayments() {

        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(EXPIRE_MINUTES);

        // 15분 지난 READY 결제 조회
        List<Payment> expiredPayments = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.READY, expireTime);

        expiredPayments.forEach(payment -> {
            // 결제는 어느 경우든 CANCELLED로 정리
            payment.updateStatus(PaymentStatus.CANCELLED);

            Reservation reservation = payment.getReservation();

            if (reservation.getStatus() == ReservationStatus.PENDING) {
                // 케이스 1) 결제 미완료 예약 → 재고 점유 방지를 위해 예약도 취소
                reservation.updateStatus(ReservationStatus.CANCELLED);

                // 타임딜 예약이었다면 soldCount 복구
                if (reservation.isTimeDealReservation()) {
                    Long timeDealId = reservation.getTimeDeal().getId();
                    int restored = timeDealRepository.restoreStock(
                            timeDealId, 1,
                            TimeDealStatus.SOLD_OUT, TimeDealStatus.ACTIVE
                    );
                    log.info("[PaymentScheduler] 타임딜 재고 복구: timeDealId={}, restored={}",
                            timeDealId, restored);
                }

                log.info("[PaymentScheduler] 결제 미완료 자동 취소: reservationId={}, orderId={}",
                        reservation.getId(), payment.getOrderId());
            } else {
                // 케이스 2) 예약은 이미 CONFIRMED(정상 결제됨) → 잉여 READY 결제만 정리, 예약은 보존
                log.info("[PaymentScheduler] 잉여 READY 결제 정리: orderId={}", payment.getOrderId());
            }
        });

        // 결제 자체를 시작 안 한 PENDING 예약
        List<Reservation> abandonedReservations = reservationRepository
                .findAbandonedReservations(expireTime);

        abandonedReservations.forEach(reservation -> {
            reservation.updateStatus(ReservationStatus.CANCELLED);

            if (reservation.isTimeDealReservation()) {
                timeDealRepository.restoreStock(
                        reservation.getTimeDeal().getId(), 1,
                        TimeDealStatus.SOLD_OUT, TimeDealStatus.ACTIVE
                );
                log.info("[PaymentScheduler] 타임딜 재고 복구(미결제): timeDealId={}",
                        reservation.getTimeDeal().getId());
            }

            log.info("[PaymentScheduler] 결제 미시작 자동 취소: reservationId={}", reservation.getId());
        });
    }

    // ── 추가 로직: 망취소 실패 건 재시도 ────────────────────────────────────

    /**
     * 망취소 실패 건을 주기적으로 재시도한다.
     *
     * 대상: cancelFailedAt IS NOT NULL AND cancelRetryCount < 3
     * 성공 시: clearCancelFailed() → 정상 CANCELLED 처리
     * 실패 시: markCancelFailed() → retryCount 증가
     * 3회 초과 시: 로그 + 수동 처리 대상으로 남김
     *              (발표 후 관리자 알림/별도 조회 API 추가 예정)
     */
    @Scheduled(fixedRate = 300000)  // 5분마다 (망취소는 일시적 네트워크 문제가 많아 여유 있게)
    @Transactional
    public void retryCancelFailedPayments() {

        List<Payment> failedCancels = paymentRepository
                .findByCancelFailedAtIsNotNullAndCancelRetryCountLessThan(MAX_CANCEL_RETRY);

        if (failedCancels.isEmpty()) return;

        log.info("[PaymentScheduler] 망취소 재시도 대상: {}건", failedCancels.size());

        failedCancels.forEach(payment -> {
            if (payment.isCancelRetryExhausted()) {
                // 3회 초과 — 수동 처리 필요 로그 (관리자가 확인해야 함)
                log.error("[PaymentScheduler] 망취소 재시도 한계 초과 — 수동 처리 필요: " +
                                "paymentId={}, paymentKey={}, retryCount={}",
                        payment.getId(), payment.getPaymentKey(), payment.getCancelRetryCount());
                return;
            }

            try {
                tossPaymentClient.cancel(payment.getPaymentKey(), "서버 오류로 인한 자동 재시도 취소");
                payment.clearCancelFailed();  // 성공 시 실패 기록 초기화
                log.info("[PaymentScheduler] 망취소 재시도 성공: paymentId={}, paymentKey={}",
                        payment.getId(), payment.getPaymentKey());

            } catch (Exception e) {
                payment.markCancelFailed();   // 실패 시 retryCount 증가
                log.warn("[PaymentScheduler] 망취소 재시도 실패 ({}/{}회): paymentId={}, reason={}",
                        payment.getCancelRetryCount(), MAX_CANCEL_RETRY,
                        payment.getId(), e.getMessage());
            }
        });
    }
}
