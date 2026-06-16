package com.back.ovengers.domain.payment.repository;

import com.back.ovengers.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservation_Id(Long reservationId);
    Optional<Payment> findByOrderId(String orderId);
}
