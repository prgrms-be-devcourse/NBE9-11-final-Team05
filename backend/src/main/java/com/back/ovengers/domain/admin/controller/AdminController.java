package com.back.ovengers.domain.admin.controller;


import com.back.ovengers.domain.camping.external.GoCampingSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GoCampingSyncService goCampingSyncService;

    @GetMapping("/init-data")
    public void init() {
        goCampingSyncService.syncInitialData();
    }

    @GetMapping("/init-images-data")
    public void initImages() {
        goCampingSyncService.syncImageData();
    }

}
