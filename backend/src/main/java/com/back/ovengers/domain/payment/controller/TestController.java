package com.back.ovengers.domain.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 프론트 완성시 제거 예정
@RestController
public class TestController {

    @GetMapping("/success")
    public String success(@RequestParam String orderId,
                          @RequestParam String paymentKey,
                          @RequestParam Integer amount) {
        return "orderId: " + orderId + ", paymentKey: " + paymentKey + ", amount: " + amount;
    }
}
