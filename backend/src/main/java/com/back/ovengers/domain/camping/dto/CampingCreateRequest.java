package com.back.ovengers.domain.camping.dto;

import jakarta.validation.constraints.NotBlank;

public record CampingCreateRequest(

        String tourNum,

        String businessNum,

        @NotBlank(message = "캠핑장 이름은 필수입니다.")
        String name,

        @NotBlank(message = "시/도는 필수입니다.")
        String region,

        @NotBlank(message = "시/군/구는 필수입니다.")
        String city,

        @NotBlank(message = "주소는 필수입니다.")
        String address

) {
}
