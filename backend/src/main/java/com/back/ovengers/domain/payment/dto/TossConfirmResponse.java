package com.back.ovengers.domain.payment.dto;

import lombok.Getter;

@Getter
public class TossConfirmResponse {
    private String paymentKey;
    private String orderId;
    private String method;
    private Integer totalAmount;
    private String approvedAt;
    private String status;
}