package com.back.ovengers.domain.payment.repository;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByReservation_Id(Long reservationId);
    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByStatusAndCreatedAtBefore(
            PaymentStatus status,
            LocalDateTime dateTime
    );
}
