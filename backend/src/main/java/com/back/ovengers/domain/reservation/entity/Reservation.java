package com.back.ovengers.domain.reservation.entity;

import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.entity.BaseEntity;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Reservation extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    /**
     * 타임딜을 통한 예약인 경우에만 값이 존재한다.
     * 일반 예약은 null.
     *
     * nullable = true이므로 DB 컬럼도 NULL 허용.
     * Reservation 삭제 시 TimeDeal은 유지되어야 하므로 cascade 없음.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_deal_id")
    private TimeDeal timeDeal;

    @Column(nullable = false)
    private String rsvNum;

    @Column(nullable = false)
    private String rsvName;

    private String rsvPhone;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer guestCount;

    @Column(nullable = false)
    private Integer rsvPrice;

    private String request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    // 결제 완료 API 에서 사용
    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    // Review에서 본인 검증 및 완료된 예약인지 검증으로 사용
    public void validateReviewAuthority(Long userId) {
        if(this.user == null || !this.user.getId().equals(userId)){
            throw new CustomException(ErrorCode.REVIEW_ACCESS_DENIED);
        }

        if(this.status != ReservationStatus.COMPLETED){
            throw new CustomException(ErrorCode.RESERVATION_NOT_COMPLETED);
        }
    }

    public boolean isTimeDealReservation() {
        return this.timeDeal != null;
    }
}
