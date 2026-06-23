package com.back.ovengers.domain.chat.entity;

import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class ChatMessage extends BaseEntity {

    private Long roomId;
    private Long senderId;

    private String senderName;
    private String senderImageUrl;

    private String content;

}
