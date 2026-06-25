package com.back.ovengers.domain.camping.event;

import com.back.ovengers.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class CampingApprovedListener {

    private final ChatService chatService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handle(CampingApprovedEvent event) {

        chatService.createOpenChatRoom(
                event.campingId(), event.campingName()
        );

    }
}
