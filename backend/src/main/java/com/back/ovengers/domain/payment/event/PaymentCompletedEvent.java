package com.back.ovengers.domain.payment.event;

public record PaymentCompletedEvent(
        Long reservationId,
        Long userId,
        Long hostId,
        String campingName
) {}
