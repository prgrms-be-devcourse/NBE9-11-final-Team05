package com.back.ovengers.domain.admin.dto;

import com.back.ovengers.global.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record AdminPendingCampingResponse(
        long pendingCampingCount,
        List<PendingCampingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static AdminPendingCampingResponse of(long pendingCampingCount, Page<PendingCampingResponse> pageData) {
        PageResponse<PendingCampingResponse> pageResponse = PageResponse.from(pageData);
        return new AdminPendingCampingResponse(
                pendingCampingCount,
                pageResponse.getContent(),
                pageResponse.getPage(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isHasNext()
        );
    }
}
