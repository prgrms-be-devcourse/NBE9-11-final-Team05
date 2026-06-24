package com.back.ovengers.domain.chat.service;

import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.entity.ChatRoom;
import com.back.ovengers.domain.chat.entity.ChatRoomMember;
import com.back.ovengers.domain.chat.enums.ChatRoomStatus;
import com.back.ovengers.domain.chat.enums.ChatRoomType;
import com.back.ovengers.domain.chat.repository.ChatMessageRepository;
import com.back.ovengers.domain.chat.repository.ChatRoomMemberRepository;
import com.back.ovengers.domain.chat.repository.ChatRoomRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.response.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    @Transactional(readOnly = true)
    public CursorResponse<ChatRoomResponse> getChatRooms(Long userId, Long cursor, int size) {

        List<ChatRoomResponse> rooms =
                chatRoomRepository.findChatRooms(
                                userId,
                                cursor,
                                PageRequest.of(0, size + 1)
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

    @Transactional(readOnly = true)
    public CursorResponse<ChatMessageResponse> getChatMessages(
            Long userId,
            Long roomId,
            Long cursor,
            int size
    ) {

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOpenChatRoom(Long campingId, String campingName) {

        chatRoomRepository
                .findByCampingIdAndTypeAndStatus(
                        campingId,
                        ChatRoomType.OPEN,
                        ChatRoomStatus.ACTIVE
                )
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.builder()
                                .campingId(campingId)
                                .name(campingName + " 오픈 채팅방")
                                .type(ChatRoomType.OPEN)
                                .status(ChatRoomStatus.ACTIVE)
                                .build()
                ));
    }

    @Transactional
    public void joinOpenChat(Long roomId, Long userId) {

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (room.getType() != ChatRoomType.OPEN) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        joinChat(roomId, userId);
    }

}
