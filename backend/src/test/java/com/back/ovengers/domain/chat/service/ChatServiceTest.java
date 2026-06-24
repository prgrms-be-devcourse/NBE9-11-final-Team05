package com.back.ovengers.domain.chat.service;


import com.back.ovengers.domain.chat.dto.ChatMessageRequest;
import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.entity.ChatMessage;
import com.back.ovengers.domain.chat.entity.ChatRoom;
import com.back.ovengers.domain.chat.repository.ChatMessageRepository;
import com.back.ovengers.domain.chat.repository.ChatRoomMemberRepository;
import com.back.ovengers.domain.chat.repository.ChatRoomRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.fixture.ChatRoomFixture;
import com.back.ovengers.fixture.UserFixture;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("메시지 전송 성공")
    void sendMessage_success() {
        // given
        Long roomId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 1L);

        ChatMessageRequest request = new ChatMessageRequest("hello");

        when(chatRoomRepository.existsById(roomId)).thenReturn(true);
        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId()))
                .thenReturn(true);

        // when
        ChatMessageResponse response =
                chatService.sendMessage(roomId, user, request);

        // then
        assertThat(response.content()).isEqualTo("hello");

        verify(chatMessageRepository, times(1))
                .save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("메시지 전송 실패 - 채팅방 멤버 아님")
    void sendMessage_fail_notMember() {
        // given
        Long roomId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 10L);

        ChatMessageRequest request = new ChatMessageRequest("hello");

        when(chatRoomRepository.existsById(roomId)).thenReturn(true);
        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId()))
                .thenReturn(false);

        // when & then
        CustomException ex = assertThrows(
                CustomException.class,
                () -> chatService.sendMessage(roomId, user, request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_ACCESS_DENIED);

        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("오픈 채팅방 입장 성공")
    void joinOpenChat_success() {
        // given
        Long roomId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 10L);

        ChatRoom room = ChatRoomFixture.openRoom(roomId);

        when(chatRoomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId()))
                .thenReturn(false);

        // when
        chatService.joinOpenChat(roomId, user.getId());

        // then
        verify(chatRoomMemberRepository, times(1))
                .save(any());
    }

    @Test
    @DisplayName("오픈 채팅방 입장 실패 - OPEN 아님")
    void joinOpenChat_fail_notOpenRoom() {
        // given
        Long roomId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 10L);

        ChatRoom room = ChatRoomFixture.directRoom(roomId);

        when(chatRoomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        // when & then
        assertThatThrownBy(() ->
                chatService.joinOpenChat(roomId, user.getId())
        ).isInstanceOf(CustomException.class);

        verify(chatRoomMemberRepository, never()).save(any());
    }

}
