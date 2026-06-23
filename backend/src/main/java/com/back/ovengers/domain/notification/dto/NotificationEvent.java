package com.back.ovengers.domain.notification.dto;

public record NotificationEvent(
        Long userId,
        NotificationResponse notification
) {
}
