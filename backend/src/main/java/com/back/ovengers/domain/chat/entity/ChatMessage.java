package com.back.ovengers.domain.chat.entity;

import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    private Long roomId;
    private Long senderId;

    private String senderName;
    private String senderImageUrl;

    private String content;

    public static ChatMessage create(Long roomId, User user, String content) {
        return ChatMessage.builder()
                .roomId(roomId)
                .senderId(user.getId())
                .senderImageUrl(user.getImageUrl())
                .content(content)
                .build();
    }

}
