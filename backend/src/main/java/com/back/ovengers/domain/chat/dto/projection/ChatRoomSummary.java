package com.back.ovengers.domain.chat.dto.projection;

import java.time.LocalDateTime;

public interface ChatRoomSummary {
    Long getId();
    String getName();
    String getContent();
    LocalDateTime getLastMessageAt();
}
