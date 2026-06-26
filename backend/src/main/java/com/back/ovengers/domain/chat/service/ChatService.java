package com.back.ovengers.domain.chat.service;

import com.back.ovengers.domain.chat.dto.ChatJoinResponse;
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
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.response.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 목록 조회
    @Transactional(readOnly = true)
    public CursorResponse<ChatRoomResponse> getChatRooms(Long userId, Long cursor, int size) {

        List<ChatRoomResponse> rooms =
                chatRoomRepository.findChatRooms(
                                userId,
                                cursor,
                                PageRequest.of(0, size + 1),
                                ChatRoomStatus.ACTIVE
                        )
                        .stream()
                        .map(ChatRoomResponse::from)
                        .toList();

        boolean hasNext = rooms.size() > size;
        rooms = hasNext ? rooms.subList(0, size) : rooms;

        Long nextCursor = rooms.isEmpty()
                ? null
                : rooms.getLast().roomId();

        return new CursorResponse<>(
                rooms,
                nextCursor,
                hasNext
        );
    }

    // 채팅 메시지 조회
    @Transactional(readOnly = true)
    public CursorResponse<ChatMessageResponse> getChatMessages(
            Long userId,
            Long roomId,
            Long cursor,
            int size
    ) {

        validateChatRoomMember(roomId, userId);

        List<ChatMessageResponse> messages =
                chatMessageRepository.findChatMessages(
                                roomId,
                                cursor,
                                PageRequest.of(0, size + 1)
                        )
                        .stream()
                        .map(ChatMessageResponse::from)
                        .toList();

        boolean hasNext = messages.size() > size;
        messages = hasNext ? messages.subList(0, size) : messages;

        Long nextCursor = messages.isEmpty() ? null : messages.getLast().messageId();

        return new CursorResponse<>(messages, nextCursor, hasNext);
    }

    // 1:1 채팅방 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDirectChatRoom(
            Long reservationId,
            Long userId,
            Long hostId,
            String campingName
    ) {

        chatRoomRepository
                .findByReservationIdAndTypeAndStatus(
                        reservationId,
                        ChatRoomType.DIRECT,
                        ChatRoomStatus.ACTIVE
                )
                .orElseGet(() -> {
                    ChatRoom newRoom = chatRoomRepository.save(
                            ChatRoom.builder()
                                    .reservationId(reservationId)
                                    .name(campingName + " 1:1 채팅방")
                                    .type(ChatRoomType.DIRECT)
                                    .status(ChatRoomStatus.ACTIVE)
                                    .build()
                    );

                    joinChat(newRoom.getId(), userId);
                    joinChat(newRoom.getId(), hostId);

                    return newRoom;
                });
    }

    // 오픈 채팅방 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOpenChatRoom(Long campingId, String campingName) {

        chatRoomRepository
                .findByCampingIdAndTypeAndStatus(
                        campingId,
                        ChatRoomType.OPEN,
                        ChatRoomStatus.ACTIVE
                )
                .orElseGet(() ->
                        chatRoomRepository.save(ChatRoom.builder()
                                .campingId(campingId)
                                .name(campingName + " 오픈 채팅방")
                                .type(ChatRoomType.OPEN)
                                .status(ChatRoomStatus.ACTIVE)
                                .build()
                        )
                );
    }

    // 오픈 채팅방 참여
    @Transactional
    public ChatJoinResponse joinOpenChat(Long campingId, Long userId) {

        ChatRoom room = chatRoomRepository
                .findByCampingIdAndTypeAndStatus(campingId, ChatRoomType.OPEN, ChatRoomStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        joinChat(room.getId(), userId);

        return new ChatJoinResponse(room.getId());
    }

    private void joinChat(Long roomId, Long userId) {
        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            chatRoomMemberRepository.save(
                    ChatRoomMember.builder()
                            .roomId(roomId)
                            .userId(userId)
                            .build()
            );
        }
    }

    // 메시지 전송
    @Transactional
    public ChatMessageResponse sendMessage(User user, ChatMessageRequest request) {
        Long roomId = request.roomId();

        validateChatRoomMember(roomId, user.getId());

        ChatMessage chatMessage = ChatMessage.create(roomId, user, request.content());
        chatMessageRepository.save(chatMessage);

        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + roomId,
                response
        );

        return response;
    }

    private void validateChatRoomMember(Long roomId, Long userId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    @Transactional(readOnly = true)
    public ChatJoinResponse getDirectRoom(Long userId, Long reservationId) {
        ChatRoom room = chatRoomRepository
                .findByReservationIdAndTypeAndStatus(
                        reservationId,
                        ChatRoomType.DIRECT,
                        ChatRoomStatus.ACTIVE
                )
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        validateChatRoomMember(room.getId(), userId);

        return new ChatJoinResponse(room.getId());

    }
}
