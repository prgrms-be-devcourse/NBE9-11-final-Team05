package com.back.ovengers.domain.camping.entity;

import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "camping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Camping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private User host;

    @Column(name = "firstImageUrl")
    private String firstImageUrl;

    private String tourNum;

    private String businessNum;

    @Column(nullable = false)
    private String name;

    private String homepage;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    private Float rating;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String phone;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String notice;

    @Enumerated(EnumType.STRING)
    private CampingStatus status;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    private LocalDateTime deletedAt;
}
