package com.back.ovengers.domain.chat.service;

import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.repository.ChatMessageRepository;
import com.back.ovengers.domain.chat.repository.ChatRoomMemberRepository;
import com.back.ovengers.domain.chat.repository.ChatRoomRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.response.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

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

    public CursorResponse<ChatMessageResponse> getChatMessages(
            Long userId,
            Long roomId,
            Long cursor,
            int size
    ) {

        if(!chatRoomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        if(!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // 메시지 가져오기
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
        messages = hasNext? messages.subList(0, size) : messages;

        Long nextCursor = messages.isEmpty()? null : messages.getLast().messageId();

        return new CursorResponse<>(
                messages,
                nextCursor,
                hasNext
        );
    }
}
