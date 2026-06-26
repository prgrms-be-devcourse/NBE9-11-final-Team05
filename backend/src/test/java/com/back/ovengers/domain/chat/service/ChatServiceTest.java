package com.back.ovengers.domain.chat.service;


import com.back.ovengers.domain.chat.dto.ChatMessageRequest;
import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.entity.ChatMessage;
import com.back.ovengers.domain.chat.entity.ChatRoom;
import com.back.ovengers.domain.chat.entity.ChatRoomMember;
import com.back.ovengers.domain.chat.enums.ChatRoomStatus;
import com.back.ovengers.domain.chat.enums.ChatRoomType;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("메시지 전송 성공")
    void sendMessage_success() {
        // given
        Long roomId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 1L);

        ChatMessageRequest request = new ChatMessageRequest(roomId, "hello");

        ChatRoom room = ChatRoomFixture.openRoom(roomId);

        ChatMessage savedMessage = ChatMessage.create(roomId, user, "hello");
        ReflectionTestUtils.setField(savedMessage, "id", 10L);

        when(chatRoomRepository.existsById(roomId))
                .thenReturn(true);

        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId()))
                .thenReturn(true);

        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenReturn(savedMessage);

        when(chatRoomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(chatRoomMemberRepository.findUserIdsByRoomId(roomId))
                .thenReturn(List.of(1L, 2L));

        // when
        ChatMessageResponse response = chatService.sendMessage(user, request);

        // then
        assertThat(response.content()).isEqualTo("hello");
        assertThat(response.senderName()).isEqualTo(user.getNickname());

        // 1. 메시지 저장
        verify(chatMessageRepository).save(any(ChatMessage.class));

        // 2. 채팅방 메시지 전송
        verify(messagingTemplate).convertAndSend(
                "/topic/chat/room/" + roomId,
                response
        );

        // 3. 유저별 채팅방 리스트 업데이트
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chat/list/1"),
                any(ChatRoomResponse.class)
        );

        verify(messagingTemplate).convertAndSend(
                eq("/topic/chat/list/2"),
                any(ChatRoomResponse.class)
        );
    }

    @Test
    @DisplayName("메시지 전송 실패 - 채팅방 멤버 아님")
    void sendMessage_fail_notMember() {
        // given
        Long roomId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 10L);

        ChatMessageRequest request = new ChatMessageRequest(roomId, "hello");

        when(chatRoomRepository.existsById(roomId)).thenReturn(true);
        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId()))
                .thenReturn(false);

        // when & then
        CustomException ex = assertThrows(
                CustomException.class,
                () -> chatService.sendMessage(user, request)
        );

        assertThat(ex.getErrorCode())
                .isEqualTo(ErrorCode.CHAT_ROOM_ACCESS_DENIED);

        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(), Optional.ofNullable(any()));
    }

    @Test
    @DisplayName("오픈 채팅방 입장 성공")
    void joinOpenChat_success() {
        // given
        Long campingId = 100L;
        Long roomId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 10L);

        ChatRoom room = ChatRoomFixture.openRoom(roomId);
        ReflectionTestUtils.setField(room, "campingId", campingId);

        when(chatRoomRepository.findByCampingIdAndTypeAndStatus(
                campingId,
                ChatRoomType.OPEN,
                ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.of(room));

        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId()))
                .thenReturn(false);

        // when
        chatService.joinOpenChat(campingId, user.getId());

        // then
        verify(chatRoomMemberRepository).save(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("오픈 채팅방 입장 실패 - 채팅방 없음")
    void joinOpenChat_fail_roomNotFound() {
        // given
        Long campingId = 100L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", 10L);

        when(chatRoomRepository.findByCampingIdAndTypeAndStatus(
                campingId,
                ChatRoomType.OPEN,
                ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(
                CustomException.class,
                () -> chatService.joinOpenChat(campingId, user.getId())
        );

        assertThat(ex.getErrorCode())
                .isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);

        verify(chatRoomMemberRepository, never()).save(any());
    }
}
