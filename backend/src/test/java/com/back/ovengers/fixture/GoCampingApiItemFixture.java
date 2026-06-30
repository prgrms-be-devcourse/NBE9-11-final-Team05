package com.back.ovengers.fixture;

import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;

public class GoCampingApiItemFixture {

    public static GoCampingApiItem create(String contentId, String facltNm) {
        return new GoCampingApiItem(
                contentId,
                "https://image.com/test.jpg",
                "trsagntNo",
                "1234567890",
                facltNm,
                "https://example.com",
                "경기도",
                "가평군",
                "경기도 가평군 테스트로 1",
                "한 줄 소개",
                "소개",
                "특징",
                "010-1234-5678",
                "전기, 와이파이",
                "계곡",
                "캠프파이어",
                "127.1234",
                "37.1234"
        );
    }
}
