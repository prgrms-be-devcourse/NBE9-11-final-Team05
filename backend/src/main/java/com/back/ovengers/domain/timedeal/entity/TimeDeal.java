package com.back.ovengers.domain.timedeal.entity;

import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_deal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeDeal extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "sold_count", nullable = false)
    private int soldCount;

    @Column(name = "original_price", nullable = false)
    private int originalPrice;

    @Column(name = "deal_price", nullable = false)
    private int dealPrice;

    @Column(name = "sale_start_at", nullable = false)
    private LocalDateTime saleStartAt;

    @Column(name = "sale_end_at", nullable = false)
    private LocalDateTime saleEndAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimeDealStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private TimeDeal(Site site, LocalDate checkIn, LocalDate checkOut, int quantity,
                     int originalPrice, int dealPrice,
                     LocalDateTime saleStartAt, LocalDateTime saleEndAt) {
        this.site = site;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.quantity = quantity;
        this.soldCount = 0;
        this.originalPrice = originalPrice;
        this.dealPrice = dealPrice;
        this.saleStartAt = saleStartAt;
        this.saleEndAt = saleEndAt;
        this.status = calculateInitialStatus(saleStartAt, saleEndAt);
    }

    private static TimeDealStatus calculateInitialStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(end)) {
            throw new IllegalArgumentException("이미 종료된 시간으로는 타임딜을 등록할 수 없습니다.");
        }
        return now.isBefore(start) ? TimeDealStatus.SCHEDULED : TimeDealStatus.ACTIVE;
    }


    public boolean isOwnedBy(Long hostId) {
        return this.site.getCamping().getHost().getId().equals(hostId);
    }

    // 판매가 시작되기 전이고(SCHEDULED), 한 건도 안 팔렸을 때만 수정 허용
    public boolean isModifiable() {
        return this.soldCount == 0
                && this.status != TimeDealStatus.CANCELLED
                && this.status != TimeDealStatus.ENDED;
    }

    public void update(int quantity, int dealPrice, LocalDateTime saleStartAt, LocalDateTime saleEndAt) {
        if (!isModifiable()) {
            throw new IllegalStateException("판매가 시작되었거나 종료/취소된 타임딜은 수정할 수 없습니다.");
        }
        if (quantity < this.soldCount) {
            throw new IllegalArgumentException("이미 판매된 수량보다 적게 설정할 수 없습니다.");
        }
        this.quantity = quantity;
        this.dealPrice = dealPrice;
        this.saleStartAt = saleStartAt;
        this.saleEndAt = saleEndAt;
        this.status = calculateInitialStatus(saleStartAt, saleEndAt);
    }

    public void cancel() {
        if (this.status == TimeDealStatus.ENDED) {
            throw new IllegalStateException("이미 종료된 타임딜은 취소할 수 없습니다.");
        }
        this.status = TimeDealStatus.CANCELLED;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 실제 구매(예약 연동) 시 호출할 훅 - 동시성 처리는 서비스단에서 별도 고려 필요
    public void increaseSoldCount(int count) {
        if (this.soldCount + count > this.quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.soldCount += count;
        if (this.soldCount >= this.quantity) {
            this.status = TimeDealStatus.SOLD_OUT;
        }
    }

    public void refreshStatusByTime() {
        if (this.status == TimeDealStatus.CANCELLED || this.status == TimeDealStatus.SOLD_OUT) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(this.saleEndAt)) {
            this.status = TimeDealStatus.ENDED;
        } else if (!now.isBefore(this.saleStartAt)) {
            this.status = TimeDealStatus.ACTIVE;
        }
    }
}
