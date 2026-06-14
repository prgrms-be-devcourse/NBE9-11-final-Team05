package com.back.ovengers.domain.site.entity;

import com.back.ovengers.domain.camping.entity.Camping;
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
}