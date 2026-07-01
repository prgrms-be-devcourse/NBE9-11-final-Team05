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

    @Test
    @DisplayName("채팅방 목록 조회 - 다음 페이지 없음")
    void getChatRooms_noNext() {
        // given
        Long userId = 1L;
        when(chatRoomRepository.findChatRooms(any(), any(), any(), any()))
                .thenReturn(List.of());

        // when
        var result = chatService.getChatRooms(userId, null, 10);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("채팅 메시지 조회 성공")
    void getChatMessages_success() {
        // given
        Long roomId = 1L;
        Long userId = 1L;

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", userId);

        ChatMessage msg = ChatMessage.create(roomId, user, "안녕하세요");
        ReflectionTestUtils.setField(msg, "id", 1L);

        when(chatRoomRepository.existsById(roomId)).thenReturn(true);
        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatMessageRepository.findChatMessages(any(), any(), any())).thenReturn(List.of(msg));

        // when
        var result = chatService.getChatMessages(userId, roomId, null, 10);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).content()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("채팅 메시지 조회 실패 - 채팅방 없음")
    void getChatMessages_fail_roomNotFound() {
        // given
        Long roomId = 999L;
        Long userId = 1L;

        when(chatRoomRepository.existsById(roomId)).thenReturn(false);

        // when & then
        CustomException ex = assertThrows(
                CustomException.class,
                () -> chatService.getChatMessages(userId, roomId, null, 10)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("채팅 메시지 조회 실패 - 멤버 아님")
    void getChatMessages_fail_notMember() {
        // given
        Long roomId = 1L;
        Long userId = 99L;

        when(chatRoomRepository.existsById(roomId)).thenReturn(true);
        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(false);

        // when & then
        CustomException ex = assertThrows(
                CustomException.class,
                () -> chatService.getChatMessages(userId, roomId, null, 10)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }

    @Test
    @DisplayName("1:1 채팅방 조회 성공")
    void getDirectRoom_success() {
        // given
        Long userId = 1L;
        Long reservationId = 100L;
        Long roomId = 1L;

        ChatRoom room = ChatRoomFixture.directRoom(roomId);
        ReflectionTestUtils.setField(room, "reservationId", reservationId);

        when(chatRoomRepository.findByReservationIdAndTypeAndStatus(
                reservationId, ChatRoomType.DIRECT, ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.of(room));
        when(chatRoomRepository.existsById(roomId)).thenReturn(true);
        when(chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);

        // when
        var result = chatService.getDirectRoom(userId, reservationId);

        // then
        assertThat(result.roomId()).isEqualTo(roomId);
    }

    @Test
    @DisplayName("1:1 채팅방 조회 실패 - 채팅방 없음")
    void getDirectRoom_fail_roomNotFound() {
        // given
        Long userId = 1L;
        Long reservationId = 999L;

        when(chatRoomRepository.findByReservationIdAndTypeAndStatus(
                reservationId, ChatRoomType.DIRECT, ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(
                CustomException.class,
                () -> chatService.getDirectRoom(userId, reservationId)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("채팅방 닫기 성공")
    void closeByReservationId_success() {
        // given
        Long reservationId = 100L;
        Long roomId = 1L;

        ChatRoom room = ChatRoomFixture.directRoom(roomId);
        ReflectionTestUtils.setField(room, "reservationId", reservationId);

        when(chatRoomRepository.findByReservationIdAndTypeAndStatus(
                reservationId, ChatRoomType.DIRECT, ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.of(room));

        // when
        chatService.closeByReservationId(reservationId);

        // then
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.CLOSED);
    }

    @Test
    @DisplayName("채팅방 닫기 실패 - 채팅방 없음")
    void closeByReservationId_fail_roomNotFound() {
        // given
        Long reservationId = 999L;

        when(chatRoomRepository.findByReservationIdAndTypeAndStatus(
                reservationId, ChatRoomType.DIRECT, ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(
                CustomException.class,
                () -> chatService.closeByReservationId(reservationId)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("오픈 채팅방 생성 - 이미 존재하면 생성 안 함")
    void createOpenChatRoom_alreadyExists() {
        // given
        Long campingId = 100L;

        ChatRoom existing = ChatRoomFixture.openRoom(1L);
        ReflectionTestUtils.setField(existing, "campingId", campingId);

        when(chatRoomRepository.findByCampingIdAndTypeAndStatus(
                campingId, ChatRoomType.OPEN, ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        // when
        chatService.createOpenChatRoom(campingId, "강릉 솔밭");

        // then
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("오픈 채팅방 생성 - 없으면 새로 생성")
    void createOpenChatRoom_createNew() {
        // given
        Long campingId = 100L;

        when(chatRoomRepository.findByCampingIdAndTypeAndStatus(
                campingId, ChatRoomType.OPEN, ChatRoomStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ChatRoom newRoom = ChatRoomFixture.openRoom(1L);
        when(chatRoomRepository.save(any())).thenReturn(newRoom);

        // when
        chatService.createOpenChatRoom(campingId, "강릉 솔밭");

        // then
        verify(chatRoomRepository).save(any());
    }
}
