package com.back.ovengers.domain.notification.service;

import com.back.ovengers.domain.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SseEmitterService sseEmitterService;

    // DB 커밋 성공 후에만 SSE 푸시 실행
    // SSE 실패해도 알림은 이미 커밋됐으므로 DB에 남아있음
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event){
        sseEmitterService.send(event.userId(), event.notification());
    }

}
