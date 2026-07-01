package com.back.ovengers.domain.notification.service;

import com.back.ovengers.domain.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SseEmitterService sseEmitterService;

    // DB 커밋 성공 후에만 SSE 푸시 실행
    // @Async로 별도 스레드에서 실행 → 예약 승인 API 응답 시간에 영향 없음
    // SSE 실패해도 알림은 이미 커밋됐으므로 DB에 남아있음
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event){
        sseEmitterService.send(event.userId(), event.notification());
    }

}
