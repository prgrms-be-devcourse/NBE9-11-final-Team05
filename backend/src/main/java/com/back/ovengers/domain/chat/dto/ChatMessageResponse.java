package com.back.ovengers.domain.chat.dto;

import com.back.ovengers.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long senderId,
        String senderName,
        String content,
        LocalDateTime createdAt
) {

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getSenderId(),
                chatMessage.getSenderName(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }

}
