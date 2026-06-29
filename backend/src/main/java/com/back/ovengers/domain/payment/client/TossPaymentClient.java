package com.back.ovengers.domain.payment.client;

import com.back.ovengers.domain.payment.dto.TossConfirmResponse;

public interface TossPaymentClient {

    TossConfirmResponse confirm(String paymentKey, String orderId, Integer amount);

    void cancel(String paymentKey, String cancelReason);
}
