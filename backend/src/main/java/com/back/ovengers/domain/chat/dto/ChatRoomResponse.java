package com.back.ovengers.domain.chat.dto;

import com.back.ovengers.domain.chat.projection.ChatRoomSummary;
import com.back.ovengers.domain.chat.enums.ChatRoomType;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long roomId,
        String roomName,
        String lastMessage,
        ChatRoomType type,
        LocalDateTime lastMessageAt
) {

    public static ChatRoomResponse from(ChatRoomSummary summary) {
        return new ChatRoomResponse(
                summary.getId(),
                summary.getName(),
                summary.getContent(),
                summary.getType(),
                summary.getLastMessageAt()
        );
    }

}
