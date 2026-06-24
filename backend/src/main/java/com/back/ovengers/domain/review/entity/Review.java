package com.back.ovengers.domain.review.entity;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.entity.BaseEntity;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "review",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_rsv_id",
                columnNames = "rsv_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camping_id", nullable = false)
    private Camping camping;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rsv_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public void validateOwner(Long userId) {
        if (this.user == null || !this.user.getId().equals(userId)) {
            throw new CustomException(ErrorCode.REVIEW_ACCESS_DENIED);
        }
    }
}
