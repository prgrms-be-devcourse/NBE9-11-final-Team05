package com.back.ovengers.domain.chat.event;

import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.domain.payment.event.PaymentCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatEventHandlerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatEventHandler chatEventHandler;

    @Test
    @DisplayName("결제 완료 이벤트 수신 시 1:1 채팅방 생성")
    void handlePaymentCompleted() {
        PaymentCompletedEvent event = new PaymentCompletedEvent(100L, 1L, 2L, "강릉 솔밭");

        chatEventHandler.handlePaymentCompleted(event);

        verify(chatService).createDirectChatRoom(100L, 1L, 2L, "강릉 솔밭");
    }
}
