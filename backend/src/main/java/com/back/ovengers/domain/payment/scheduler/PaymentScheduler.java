package com.back.ovengers.domain.payment.scheduler;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
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
            payment.getReservation().updateStatus(ReservationStatus.CANCELLED);
            log.info("결제 미완료로 자동 취소 처리: orderId={}", payment.getOrderId());
        });
    }
}
