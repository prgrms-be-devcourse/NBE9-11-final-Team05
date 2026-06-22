package com.back.ovengers.domain.timedeal.entity;

public enum TimeDealStatus {
    SCHEDULED, // 판매 예정
    ACTIVE,    // 판매중
    SOLD_OUT,  // 매진
    ENDED,     // 판매 종료(시간 만료)
    CANCELLED  // 호스트가 취소
}
