package com.back.ovengers.domain.payment.scheduler;

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

    private static final int EXPIRE_MINUTES = 30;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirePendingPayments() {

        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(EXPIRE_MINUTES);

        List<Payment> expiredPayments = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.READY, expireTime);

        if (expiredPayments.isEmpty()) {
            return;
        }

        expiredPayments.forEach(payment -> {
            payment.updateStatus(PaymentStatus.CANCELLED);

            Reservation reservation = payment.getReservation();
            reservation.updateStatus(ReservationStatus.CANCELLED);

            // 타임딜 예약이었다면 soldCount 복구
            if (reservation.isTimeDealReservation()) {
                Long timeDealId = reservation.getTimeDeal().getId();
                int restored = timeDealRepository.restoreStock(
                        timeDealId,
                        1,
                        TimeDealStatus.SOLD_OUT,
                        TimeDealStatus.ACTIVE
                );
                log.info("[PaymentScheduler] 타임딜 재고 복구: timeDealId={}, restored={}",
                        timeDealId, restored);
            }

            log.info("[PaymentScheduler] 결제 미완료 자동 취소: orderId={}", payment.getOrderId());
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
}
