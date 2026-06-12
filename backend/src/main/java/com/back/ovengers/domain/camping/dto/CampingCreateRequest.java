package com.back.ovengers.domain.camping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampingCreateRequest {

    private String tourNum;

    private String businessNum;

    @NotBlank(message = "캠핑장 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "시/도는 필수입니다.")
    private String region;

    @NotBlank(message = "시/군/구는 필수입니다.")
    private String city;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private BigDecimal lat;

    private BigDecimal lng;
}
