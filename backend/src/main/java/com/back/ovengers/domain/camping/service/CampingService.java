package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingDetailResponse;
import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.dto.CampingSearchResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.projection.ReservedSiteCount;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.review.repository.ReviewRepository;
import com.back.ovengers.domain.site.dto.SiteResponse;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampingService {

    private final CampingRepository campingRepository;
    private final CampingImageRepository campingImageRepository;
    private final SiteRepository siteRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public Page<CampingListResponse> getCampList(String keyword, Pageable pageable) {

        String keywordLike = !StringUtils.hasText(keyword) ? null : "%" + keyword + "%";
        Page<Camping> pagedCamps = campingRepository.searchApprovedCamping(CampingStatus.APPROVED, keywordLike, pageable);

        return pagedCamps.map(CampingListResponse::from);
    }

    @Transactional(readOnly = true)
    public CampingDetailResponse getCampDetail(Long campingId) {
        Camping camp = campingRepository.findById(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        List<String> imageUrls = campingImageRepository.findByCampingId(campingId)
                .stream()
                .map(CampingImage::getImageUrl)
                .toList();

        List<SiteResponse> sites = siteRepository.findByCampingId(campingId)
                .stream()
                .map(SiteResponse::from)
                .toList();

        return CampingDetailResponse.from(camp, imageUrls, sites);
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> getAvailableSites(
            Long campingId,
            LocalDate checkIn,
            LocalDate checkOut) {

        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
        );

        List<Site> sites = siteRepository.findByCampingId(campingId);

        List<ReservedSiteCount> counts =
                reservationRepository.findReservedCountByCampingAndPeriod(
                        campingId,
                        checkIn,
                        checkOut,
                        activeStatuses
                );

        Map<Long, Long> reservedMap = counts.stream()
                .collect(Collectors.toMap(
                        ReservedSiteCount::getSiteId,
                        ReservedSiteCount::getCount
                ));

        return sites.stream()
                .filter(site -> {
                    long reserved = reservedMap.getOrDefault(site.getId(), 0L);
                    return site.getTotalAmount() > reserved;
                })
                .map(SiteResponse::from)
                .toList();
    }

    // 사용 가능한 캠핑장 검색전용 메서드
    @Transactional(readOnly = true)
    public Page<CampingSearchResponse> searchAvailableCampings(
            String keyword,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guestCount,
            Integer roomCount,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        // 조건에 맞는 캠핑장 목록 조회
        Page<Camping> campings = campingRepository.searchAvailableCampings(
                keyword, checkIn, checkOut, guestCount, roomCount, minPrice, maxPrice, pageable
        );

        if (campings.isEmpty()) {
            return Page.empty(pageable);
        }

        // 캠핑장 ID 목록 추출
        List<Long> campingIds = campings.getContent().stream()
                .map(Camping::getId)
                .toList();

        // 이미지 목록 한 번에 조회
        Map<Long, List<String>> imageMap = campingImageRepository.findImagesByCampingIds(campingIds)
                .stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],
                        Collectors.mapping(row -> (String) row[1], Collectors.toList())
                ));

        // 평균 별점, 리뷰 수 한 번에 조회
        // Object[0] = camping_id, Object[1] = avg_rating, Object[2] = review_count
        Map<Long, double[]> ratingMap = reviewRepository.findRatingStatsByCampingIds(campingIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> new double[]{
                                row[1] != null ? ((Number) row[1]).doubleValue() : 0.0,
                                ((Number) row[2]).doubleValue()
                        }
                ));

        // 최저가 한 번에 조회
        // guestCount 조건을 함께 전달해 검색 조건에 맞는 Site 기준 최저가 계산
        // Object[0] = camping_id, Object[1] = min_price
        Map<Long, Integer> priceMap = siteRepository.findMinPriceByCampingIds(campingIds, guestCount)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        // CampingSearchResponse 조립
        return campings.map(camping -> {
            List<String> images = imageMap.getOrDefault(camping.getId(), List.of());

            double[] ratingStats = ratingMap.getOrDefault(camping.getId(), new double[]{0.0, 0.0});
            double avgRating = ratingStats[0];
            long reviewCount = (long) ratingStats[1];

            // 사이트 없는 경우 대비 기본값 0
            Integer price = priceMap.getOrDefault(camping.getId(), 0);

            return CampingSearchResponse.of(
                    camping,
                    images,
                    avgRating,
                    reviewCount,
                    price
            );
        });
    }

}
