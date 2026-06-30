package com.back.ovengers.domain.user.controller;

import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.s3.S3Service;
import com.back.ovengers.global.s3.S3UploadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ImageController imageController;

    @Test
    void 이미지_업로드_성공_S3_URL을_반환한다() {
        // given
        MultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                "image/jpeg",
                "fake-image".getBytes()
        );

        S3UploadResult mockResult = new S3UploadResult(
                "campings/profiles/uuid.jpg",
                "https://cloudfront.example.com/campings/profiles/uuid.jpg"
        );

        given(s3Service.upload(any(MultipartFile.class), eq("campings/profiles")))
                .willReturn(mockResult);

        // when
        ResponseEntity<?> response = imageController.upload(file);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        Map<?, ?> data = (Map<?, ?>) body.get("data");
        assertThat(data.get("url"))
                .isEqualTo("https://cloudfront.example.com/campings/profiles/uuid.jpg");

        verify(s3Service).upload(any(MultipartFile.class), eq("campings/profiles"));
    }

    @Test
    void 이미지_업로드_실패_빈_파일이면_CustomException이_발생한다() {
        // given
        MultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]  // 빈 파일
        );

        given(s3Service.upload(any(MultipartFile.class), eq("campings/profiles")))
                .willThrow(new CustomException(ErrorCode.MISSING_REQUIRED_FIELD));

        // when & then
        assertThatThrownBy(() -> imageController.upload(emptyFile))
                .isInstanceOf(CustomException.class);

        verify(s3Service).upload(any(MultipartFile.class), eq("campings/profiles"));
    }

    @Test
    void 이미지_업로드_실패_이미지가_아닌_파일이면_CustomException이_발생한다() {
        // given
        MultipartFile textFile = new MockMultipartFile(
                "image",
                "document.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        given(s3Service.upload(any(MultipartFile.class), eq("campings/profiles")))
                .willThrow(new CustomException(ErrorCode.INVALID_FILE_TYPE));

        // when & then
        assertThatThrownBy(() -> imageController.upload(textFile))
                .isInstanceOf(CustomException.class);

        verify(s3Service).upload(any(MultipartFile.class), eq("campings/profiles"));
    }

    @Test
    void 이미지_업로드_실패_S3_오류시_CustomException이_발생한다() {
        // given
        MultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                "image/jpeg",
                "fake-image".getBytes()
        );

        given(s3Service.upload(any(MultipartFile.class), eq("campings/profiles")))
                .willThrow(new CustomException(ErrorCode.FILE_UPLOAD_FAILED));

        // when & then
        assertThatThrownBy(() -> imageController.upload(file))
                .isInstanceOf(CustomException.class);

        verify(s3Service).upload(any(MultipartFile.class), eq("campings/profiles"));
    }
}