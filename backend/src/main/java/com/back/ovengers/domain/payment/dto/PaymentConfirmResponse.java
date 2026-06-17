package com.back.ovengers.domain.payment.dto;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class PaymentConfirmResponse {
    private String orderId;
    private String rsvNo;
    private String campingName;
    private String siteName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer nights;
    private Integer guestCount;
    private String rsvName;
    private String rsvPhone;
    private Integer amount;
    private String paymentMethod;
    private String approvedAt;

    public static PaymentConfirmResponse of(Payment payment, String paymentMethod, String approvedAt) {
        Reservation reservation = payment.getReservation();
        return PaymentConfirmResponse.builder()
                .orderId(payment.getOrderId())
                .rsvNo(reservation.getRsvNum())
                .campingName(reservation.getSite().getCamping().getName())
                .siteName(reservation.getSite().getName())
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .nights((int) ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut()))
                .guestCount(reservation.getGuestCount())
                .rsvName(reservation.getRsvName())
                .rsvPhone(reservation.getRsvPhone())
                .amount(payment.getPaidPrice())
                .paymentMethod(paymentMethod)
                .approvedAt(approvedAt)
                .build();
    }
}