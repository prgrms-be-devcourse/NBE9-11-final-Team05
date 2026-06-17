package com.back.ovengers.fixture;

import com.back.ovengers.domain.site.entity.Site;

public class SiteFixture {

    public static Site.SiteBuilder builder() {
        return Site.builder()
                .camping(CampingFixture.builder().build())
                .name("A구역")
                .description("테스트 구역")
                .baseCapacity(2)
                .maxCapacity(4)
                .totalAmount(10)
                .price(50000);
    }

}
