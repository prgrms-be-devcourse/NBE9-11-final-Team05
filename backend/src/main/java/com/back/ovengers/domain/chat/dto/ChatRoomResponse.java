package com.back.ovengers.domain.chat.dto;

import com.back.ovengers.domain.chat.dto.projection.ChatRoomSummary;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long roomId,
        String roomName,
        String lastMessage,
        LocalDateTime lastMessageAt
) {

    public static ChatRoomResponse from(ChatRoomSummary summary) {
        return new ChatRoomResponse(
                summary.getId(),
                summary.getName(),
                summary.getContent(),
                summary.getLastMessageAt()
        );
    }

}
