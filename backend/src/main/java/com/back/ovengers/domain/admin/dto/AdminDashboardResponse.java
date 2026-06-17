package com.back.ovengers.domain.admin.dto;

public record AdminDashboardResponse(
        long totalSalesAmount,
        long activeUserCount,
        long pendingCampingCount
) {
    public static AdminDashboardResponse of(long totalSalesAmount, long activeUserCount, long pendingCampingCount) {
        return new AdminDashboardResponse(totalSalesAmount, activeUserCount, pendingCampingCount);
    }
}
