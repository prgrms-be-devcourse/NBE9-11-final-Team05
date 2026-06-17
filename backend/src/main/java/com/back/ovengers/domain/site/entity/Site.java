package com.back.ovengers.domain.site.entity;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.site.dto.SiteCreateRequest;
import com.back.ovengers.domain.site.dto.SiteUpdateRequest;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "site")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Site extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camping_id", nullable = false)
    private Camping camping;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer baseCapacity;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false)
    private Integer price;

    private LocalDateTime deletedAt;

    // site 재고
    public boolean isDeleted() {
        return deletedAt != null;
    }

    public static Site create(Camping camping, SiteCreateRequest request) {
        return Site.builder()
                .camping(camping)
                .name(request.name())
                .description(request.description())
                .baseCapacity(request.baseCapacity())
                .maxCapacity(request.maxCapacity())
                .totalAmount(request.totalAmount())
                .price(request.price())
                .build();
    }

    public void update(SiteUpdateRequest request) {
        if (request.name() != null) this.name = request.name();
        if (request.description() != null) this.description = request.description();
        if (request.baseCapacity() != null) this.baseCapacity = request.baseCapacity();
        if (request.maxCapacity() != null) this.maxCapacity = request.maxCapacity();
        if (request.totalAmount() != null) this.totalAmount = request.totalAmount();
        if (request.price() != null) this.price = request.price();
    }
}