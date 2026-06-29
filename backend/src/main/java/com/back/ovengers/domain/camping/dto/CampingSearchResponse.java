package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.Camping;

import java.util.List;

public record CampingSearchResponse(
        Long id,
        String name,
        String region,
        String city,
        String address,
        List<String> images,        // 이미지 슬라이더용
        Double averageRating,       // 리뷰 평균 별점
        Long reviewCount,           // 리뷰 수
        Integer minPrice            // Site 최저가
) {
    public static CampingSearchResponse of(
            Camping camping,
            List<String> images,
            Double averageRating,
            Long reviewCount,
            Integer minPrice
    ) {
        return new CampingSearchResponse(
                camping.getId(),
                camping.getName(),
                camping.getRegion(),
                camping.getCity(),
                camping.getAddress(),
                images,
                averageRating,
                reviewCount,
                minPrice
        );
    }
}