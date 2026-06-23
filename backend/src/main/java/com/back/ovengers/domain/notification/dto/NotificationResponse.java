package com.back.ovengers.domain.notification.dto;

import com.back.ovengers.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String type,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
