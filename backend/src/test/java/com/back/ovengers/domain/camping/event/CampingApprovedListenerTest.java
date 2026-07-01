package com.back.ovengers.domain.camping.event;

import com.back.ovengers.domain.chat.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CampingApprovedListenerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private CampingApprovedListener campingApprovedListener;

    @Test
    @DisplayName("캠핑장 승인 이벤트 수신 시 오픈 채팅방 생성")
    void handle_createsOpenChatRoom() {
        CampingApprovedEvent event = new CampingApprovedEvent(1L, "강릉 솔밭");

        campingApprovedListener.handle(event);

        verify(chatService).createOpenChatRoom(1L, "강릉 솔밭");
    }
}
