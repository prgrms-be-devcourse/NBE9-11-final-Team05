package com.back.ovengers.domain.payment.entity;

import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private String orderId;

    @Column(unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private Integer paidPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 망취소 실패 기록
    // null = 실패 없음, 값 있음 = 마지막 실패 시각
    private LocalDateTime cancelFailedAt;

    // 재시도 횟수 (최대 3회 후 포기하고 수동 처리 대상으로 남김)
    @Column(columnDefinition = "int default 0")
    @Builder.Default
    private int cancelRetryCount = 0;

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    public void confirm(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.DONE;
    }

    // 망취소 실패 기록
    public void markCancelFailed() {
        this.cancelFailedAt = LocalDateTime.now();
        this.cancelRetryCount++;
    }

    // 재시도 성공 시 실패 기록 초기화
    public void clearCancelFailed() {
        this.cancelFailedAt = null;
        this.cancelRetryCount = 0;
    }

    // 재시도 한계(3회) 초과 여부
    public boolean isCancelRetryExhausted() {
        return this.cancelRetryCount >= 3;
    }
}
