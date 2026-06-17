package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.CampingImage;

public record CampingImageCreateResponse(
        Long imageId,
        String imageUrl
) {
    public static CampingImageCreateResponse from(CampingImage image) {
        return new CampingImageCreateResponse(
                image.getId(),
                image.getImageUrl()
        );
    }
}