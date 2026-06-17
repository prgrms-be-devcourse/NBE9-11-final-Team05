package com.back.ovengers.fixture;

import com.back.ovengers.domain.camping.entity.CampingImage;

public class CampingImageFixture {

    public static CampingImage.CampingImageBuilder builder() {
        return CampingImage.builder()
                .camping(CampingFixture.builder().build())
                .imageUrl("https://image.com/test.jpg");
    }

}
