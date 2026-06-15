package com.back.ovengers.domain.camping.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoCampingApiItem(
        String contentId,
        String firstImageUrl,
        String trsagntNo,
        String bizrno,
        String facltNm,
        String homepage,
        String doNm,
        String sigunguNm,
        String addr1,
        String lineIntro,
        String intro,
        String featureNm,
        String tel,
        String sbrsCl,   // 부대시설
        String posblFcltyCl,    // 주변이용가능시설
        String exprnProgrm, // 체험프로그램명
        String mapX,
        String mapY
) {}
