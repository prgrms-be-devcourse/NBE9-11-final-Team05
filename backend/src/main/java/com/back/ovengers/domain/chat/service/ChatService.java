package com.back.ovengers.domain.chat.service;

import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.repository.ChatRoomRepository;
import com.back.ovengers.global.response.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

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
}
