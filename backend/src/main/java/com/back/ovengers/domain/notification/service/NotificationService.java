package com.back.ovengers.domain.notification.service;

import com.back.ovengers.domain.notification.dto.NotificationResponse;
import com.back.ovengers.domain.notification.dto.UnreadCountResponse;
import com.back.ovengers.domain.notification.entity.Notification;
import com.back.ovengers.domain.notification.entity.NotificationType;
import com.back.ovengers.domain.notification.repository.NotificationRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    // 알람 생성 및 SSE 푸시
    public void send(User receiver, NotificationType type, String content){
        // DB에 알림 저장
        Notification notification = Notification.builder()
                .user(receiver)
                .type(type)
                .content(content)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // SSE 푸시
        sseEmitterService.send(receiver.getId(), NotificationResponse.from(notification));
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable){
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    // 안 읽은 알람 개수 조회
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId){
        return new UnreadCountResponse(notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    // 단건 읽음 처리
    public void read(Long userId, Long notificationId){
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.validOwner(userId);
        notification.read();
    }

    // 전체 읽음 처리
    public void readAll(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}


