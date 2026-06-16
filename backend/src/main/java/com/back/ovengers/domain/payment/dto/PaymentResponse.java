package com.back.ovengers.domain.payment.dto;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class PaymentResponse {
    private String orderId;
    private String orderName;
    private Integer amount;
    private String customerName;

    public static PaymentResponse of(Payment payment, Reservation reservation) {
        return PaymentResponse.builder()
                .orderId(payment.getOrderId())
                .orderName(reservation.getSite().getCamping().getName()
                        + " "
                        + ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut())
                        + "박")
                .amount(payment.getPaidPrice())
                .customerName(reservation.getRsvName())
                .build();
    }
}