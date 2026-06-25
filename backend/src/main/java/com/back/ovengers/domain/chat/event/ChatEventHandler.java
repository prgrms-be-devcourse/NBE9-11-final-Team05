package com.back.ovengers.domain.chat.event;

import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatEventHandler {

    private final ChatService chatService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        chatService.createDirectChatRoom(
                event.reservationId(),
                event.userId(),
                event.hostId(),
                event.campingName()
        );
    }
}
