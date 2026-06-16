package com.back.ovengers.domain.payment.entity;

import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private String orderId;

    private String paymentKey;

    @Column(nullable = false)
    private Integer paidPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
}