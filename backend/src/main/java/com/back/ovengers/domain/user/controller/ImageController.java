package com.back.ovengers.domain.user.controller;

import com.back.ovengers.global.s3.S3Service;
import com.back.ovengers.global.s3.S3UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile file) {
        S3UploadResult result = s3Service.upload(file, "campings/profiles");
        // 프론트 uploadImage()가 data.data.url로 파싱
        return ResponseEntity.ok(Map.of("data", Map.of("url", result.imageUrl())));
    }
}