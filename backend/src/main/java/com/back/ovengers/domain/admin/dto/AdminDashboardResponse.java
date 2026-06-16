package com.back.ovengers.domain.admin.dto;

public record AdminDashboardResponse(
        Long totalSalesAmount,
        Long activeUserCount,
        Long pendingCampingCount
) {
    public static AdminDashboardResponse of(Long totalSalesAmount, Long activeUserCount, Long pendingCampingCount) {
        return new AdminDashboardResponse(totalSalesAmount, activeUserCount, pendingCampingCount);
    }
}
