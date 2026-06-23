package com.back.ovengers.domain.settlement.entity;

import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Settlement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false)
    private Integer feeAmount;

    @Column(nullable = false)
    private Integer payoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
    }
}
