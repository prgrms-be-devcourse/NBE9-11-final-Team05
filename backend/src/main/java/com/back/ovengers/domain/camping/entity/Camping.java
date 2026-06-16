package com.back.ovengers.domain.camping.entity;


import com.back.ovengers.domain.camping.dto.CampingUpdateRequest;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.user.entity.User;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

    @Column(name = "content_id", unique = true)
    private Long contentId;

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

    public void update(CampingUpdateRequest request) {
        this.firstImageUrl = request.firstImageUrl();
        this.name = request.name();
        this.homepage = request.homepage();
        this.region = request.region();
        this.city = request.city();
        this.address = request.address();
        this.description = request.description();
        this.phone = request.phone();
        this.checkInTime = request.checkInTime();
        this.checkOutTime = request.checkOutTime();
        this.notice = request.notice();
        this.lat = request.lat();
        this.lng = request.lng();
    }
  
    public static Camping from(GoCampingApiItem item) {
        return Camping.builder()
                .contentId(Long.valueOf(item.contentId()))
                .firstImageUrl(item.firstImageUrl())
                .tourNum(item.trsagntNo())
                .businessNum(item.bizrno())
                .name(item.facltNm())
                .homepage(item.homepage())
                .region(item.doNm())
                .city(item.sigunguNm())
                .address(item.addr1())
                .description("%s\n%s\n%s".formatted(item.lineIntro(), item.intro(), item.featureNm()))
                .phone(item.tel())
                .notice("부대시설: %s\n주변이용가능시설: %s\n체험프로그램명: %s".formatted(item.sbrsCl(), item.posblFcltyCl(), item.exprnProgrm()))
                .status(CampingStatus.APPROVED)
                .lat(toBigDecimal(item.mapY()))
                .lng(toBigDecimal(item.mapX()))
                .build();
    }

    private static BigDecimal toBigDecimal(String value) {
        return StringUtils.isBlank(value)
                ? null
                : new BigDecimal(value);
    }

}
