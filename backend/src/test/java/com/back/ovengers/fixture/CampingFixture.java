package com.back.ovengers.fixture;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;

public class CampingFixture {
    public static Camping.CampingBuilder builder() {
        return Camping.builder()
                .contentId(1L)
                .firstImageUrl("https://image.com/test.jpg")
                .name("테스트캠핑장")
                .region("대구광역시")
                .city("군위군")
                .address("대구광역시 군위군 부계면 한티로")
                .rating(4.5f)
                .status(CampingStatus.APPROVED);
    }
}
