package com.back.ovengers.domain.chat.entity;

import com.back.ovengers.domain.chat.enums.ChatRoomStatus;
import com.back.ovengers.domain.chat.enums.ChatRoomType;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class ChatRoom extends BaseEntity {

    private Long reservationId;

    private Long campingId;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;

    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status;

}
