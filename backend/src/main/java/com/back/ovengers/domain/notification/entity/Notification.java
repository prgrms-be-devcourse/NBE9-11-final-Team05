package com.back.ovengers.domain.notification.entity;

import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.entity.BaseEntity;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_user_created", columnList = "user_id, created_at DESC")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isRead;

    public void read(){
        this.isRead = true;
    }

    public void validOwner(Long userId){
        if(!this.user.getId().equals(userId)){
            throw new CustomException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }
    }
}
