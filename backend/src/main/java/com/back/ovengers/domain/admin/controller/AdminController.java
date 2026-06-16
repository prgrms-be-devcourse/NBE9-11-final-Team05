package com.back.ovengers.domain.admin.controller;


import com.back.ovengers.domain.admin.dto.AdminDashboardResponse;
import com.back.ovengers.domain.admin.service.AdminService;
import com.back.ovengers.domain.camping.external.GoCampingSyncService;
import com.back.ovengers.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GoCampingSyncService goCampingSyncService;
    private final AdminService adminService;

    @GetMapping("/init-data")
    public void init() {
        goCampingSyncService.syncInitialData();
    }

    @GetMapping("/init-images-data")
    public void initImages() {
        goCampingSyncService.syncImageData();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse response = adminService.getDashboard();
        return ResponseEntity.ok(new ApiResponse<>(
                "관리자 대시보드 조회 성공",
                response
        ));
    }

}
