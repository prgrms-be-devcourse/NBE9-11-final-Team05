package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.CampingImage;

public record CampingImageCreateResponse(
        Long imageId,
        String imageUrl,
        boolean thumbnail
) {
    public static CampingImageCreateResponse from(
            CampingImage image,
            String firstImageUrl
    ) {
        return new CampingImageCreateResponse(
                image.getId(),
                image.getImageUrl(),
                image.getImageUrl().equals(firstImageUrl)
        );
    }
}