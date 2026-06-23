package com.back.ovengers.domain.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long roomId,
        String roomName,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}
