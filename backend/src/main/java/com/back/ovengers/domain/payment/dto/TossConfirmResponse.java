package com.back.ovengers.domain.payment.dto;

public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        String method,
        Integer totalAmount,
        String approvedAt,
        String status
) {}