package com.back.ovengers.domain.notification.entity;

public enum NotificationType {
    CAMPING_APPROVED,       // 캠핑장 승인
    CAMPING_REJECTED,       // 캠핑장 거절
    PAYMENT_DONE,           // 결제 완료
    RESERVATION_CONFIRMED,  // 예약 확정
    REVIEW_CREATED          // 새 리뷰 (내 캠핑장)
}
