package com.back.ovengers.domain.user.controller;

import com.back.ovengers.global.s3.S3Service;
import com.back.ovengers.global.s3.S3UploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Image", description = "프로필 이미지 업로드 API")
public class ImageController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    @Operation(
            summary = "이미지 업로드",
            description = "이미지를 S3에 업로드하고 업로드된 이미지 URL을 반환합니다."
    )
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile file) {
        S3UploadResult result = s3Service.upload(file, "campings/profiles");
        // 프론트 uploadImage()가 data.data.url로 파싱
        return ResponseEntity.ok(Map.of("data", Map.of("url", result.imageUrl())));
    }
}
