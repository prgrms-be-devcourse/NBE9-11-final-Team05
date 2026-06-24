package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.CampingImageCreateResponse;
import com.back.ovengers.domain.camping.service.CampingImageService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Host Camping", description = "호스트 캠핑장 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host/campings")
@Validated
public class CampingImageController {

    private final CampingImageService campingImageService;

    @Operation(summary = "캠핑장 이미지 등록")
    @PostMapping(
            value = "/{campingId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CampingImageCreateResponse>> addCampingImage(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캠핑장 ID", example = "1")
            @PathVariable @Min(1) Long campingId,
            @RequestPart("image") MultipartFile image,
            @RequestParam(defaultValue = "false") boolean thumbnail
    ) {
        CampingImageCreateResponse response =
                campingImageService.addCampingImage(
                        user.getId(),
                        campingId,
                        image,
                        thumbnail
                );

        return ResponseEntity.ok(
                new ApiResponse<>("캠핑장 이미지가 등록되었습니다.", response)
        );
    }

    @Operation(summary = "캠핑장 이미지 삭제")
    @DeleteMapping("/{campingId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteCampingImage(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캠핑장 ID", example = "1")
            @PathVariable @Min(1) Long campingId,
            @Parameter(description = "이미지 ID", example = "1")
            @PathVariable @Min(1) Long imageId
    ) {
        campingImageService.deleteCampingImage(
                user.getId(),
                campingId,
                imageId
        );

        return ResponseEntity.ok(
                new ApiResponse<>("캠핑장 이미지가 삭제되었습니다.", null)
        );
    }
}
