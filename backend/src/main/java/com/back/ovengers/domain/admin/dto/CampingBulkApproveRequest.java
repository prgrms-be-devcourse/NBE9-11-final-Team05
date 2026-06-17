package com.back.ovengers.domain.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CampingBulkApproveRequest(
        @NotEmpty(message = "승인할 캠핑장을 선택해주세요.")
        List<Long> campingIds
) {
}
