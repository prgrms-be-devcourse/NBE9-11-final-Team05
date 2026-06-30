package com.back.ovengers.domain.chat.projection;

import com.back.ovengers.domain.chat.enums.ChatRoomType;

import java.time.LocalDateTime;

public interface ChatRoomSummary {
    Long getId();

    String getName();

    String getContent();

    ChatRoomType getType();

    LocalDateTime getLastMessageAt();
}
