package com.back.ovengers.domain.reservation.scheduler;

import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCompleteScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(cron = "0 0 11 * * *") // 매일 오전 11시 실행
    @Transactional
    public void completeExpiredReservations() {
        LocalDate today = LocalDate.now();

        int updatedCount = reservationRepository.bulkUpdateStatusByStatusAndCheckOutBefore(
                ReservationStatus.COMPLETED,
                ReservationStatus.CONFIRMED,
                today.plusDays(1)
        );

        if (updatedCount > 0) {
            log.info("이용 완료 처리 완료: {}건", updatedCount);
        }
    }
}
