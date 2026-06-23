package com.back.ovengers.domain.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long senderId,
        String senderName,
        String content,
        LocalDateTime createdAt
) {}
