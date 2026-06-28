package com.back.ovengers.domain.camping.entity;


import com.back.ovengers.domain.camping.dto.CampingCreateRequest;
import com.back.ovengers.domain.camping.dto.CampingUpdateRequest;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.entity.BaseEntity;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.*;

import java.awt.*;
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

    @Enumerated(EnumType.STRING)
    private ImageSyncStatus imageSyncStatus;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    private LocalDateTime deletedAt;

    public static Camping create(User host, CampingCreateRequest request) {
        return Camping.builder()
                .host(host)
                .tourNum(request.tourNum())
                .businessNum(request.businessNum())
                .name(request.name())
                .region(request.region())
                .city(request.city())
                .address(request.address())
                .status(CampingStatus.PENDING)
                .imageSyncStatus(ImageSyncStatus.DONE)
                .build();
    }

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

    public void updateImageSyncStatus(ImageSyncStatus imageSyncStatus) {
        this.imageSyncStatus = imageSyncStatus;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void changeFirstImageUrl(String firstImageUrl) {
        this.firstImageUrl = firstImageUrl;
    }

    public static Camping from(GoCampingApiItem item) {
        Long contentId = item.contentId() != null
                ? Long.valueOf(item.contentId())
                : null;

        ImageSyncStatus imageStatus = contentId == null
                ? ImageSyncStatus.NO_CONTENT
                : ImageSyncStatus.PENDING;

        return Camping.builder()
                .contentId(contentId)
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
                .imageSyncStatus(imageStatus)
                .lat(toBigDecimal(item.mapY()))
                .lng(toBigDecimal(item.mapX()))
                .build();
    }

    private static BigDecimal toBigDecimal(String value) {
        return StringUtils.isBlank(value)
                ? null
                : new BigDecimal(value);
    }


    public void approve() {
        if (this.status != CampingStatus.PENDING) {
            throw new CustomException(ErrorCode.CAMPING_NOT_PENDING);
        }
        this.status = CampingStatus.APPROVED;
    }

    public void reject() {
        if (this.status != CampingStatus.PENDING) {
            throw new CustomException(ErrorCode.CAMPING_NOT_PENDING);
        }
        this.status = CampingStatus.REJECTED;
    }

    public void assignHost(User host) {
        // 한 캠핑장은 하나의 호스트에게만 소유권을 부여할 수 있음
        if (this.host != null) {
            throw new CustomException(ErrorCode.CAMPING_ALREADY_CLAIMED);
        }

        this.host = host;
    }
}
