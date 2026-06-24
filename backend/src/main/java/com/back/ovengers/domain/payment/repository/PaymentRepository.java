package com.back.ovengers.domain.payment.repository;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByReservation_Id(Long reservationId);
    Optional<Payment> findByOrderId(String orderId);

    // 관리자 대시보드
    // COALESCE(값, 0)은 첫 번째 값이 NULL이면 두 번째 값(0)을 대신 반환
    @Query("SELECT COALESCE(SUM(p.paidPrice), 0L) FROM Payment p WHERE p.status = :status")
    long getTotalSalesAmount(@Param("status") PaymentStatus status);

    List<Payment> findByStatusAndCreatedAtBefore(
            PaymentStatus status,
            LocalDateTime dateTime
    );

    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.reservation r
        JOIN FETCH r.site s
        JOIN FETCH s.camping c
        JOIN FETCH c.host h
        WHERE p.status = :status
        AND CAST(p.createdAt AS date) BETWEEN :startDate AND :endDate
        AND p.id NOT IN (
            SELECT sd.payment.id FROM SettlementDetail sd
        )
        """)
    List<Payment> findAllSettlementTargets(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") PaymentStatus status
    );
}
