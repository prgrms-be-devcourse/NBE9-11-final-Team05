package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.HostCampingImageResponse;
import com.back.ovengers.domain.camping.service.CampingImageService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.security.JwtFilter;
import com.back.ovengers.global.security.JwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CampingImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class CampingImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampingImageService campingImageService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final Long USER_ID = 1L;
    private static final Long CAMPING_ID = 1L;
    private static final Long IMAGE_ID = 1L;

    private RequestPostProcessor hostUser;

    @BeforeEach
    void setUp() {
        User user = mock(User.class);
        given(user.getId()).willReturn(USER_ID);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_HOST"))
                );

        hostUser = request -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return request;
        };
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("캠핑장 이미지 등록 성공")
    void addCampingImage_success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test-image-content".getBytes()
        );

        HostCampingImageResponse response = new HostCampingImageResponse(
                IMAGE_ID,
                "https://image.com/test.jpg",
                false
        );

        given(campingImageService.addCampingImage(eq(USER_ID), eq(CAMPING_ID), any(MultipartFile.class), eq(false)))
                .willReturn(response);

        mockMvc.perform(multipart("/api/host/campings/{campingId}/images", CAMPING_ID)
                        .file(image)
                        .with(hostUser)
                        .param("thumbnail", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 이미지가 등록되었습니다."))
                .andExpect(jsonPath("$.data.imageId").value(IMAGE_ID))
                .andExpect(jsonPath("$.data.imageUrl").value("https://image.com/test.jpg"))
                .andExpect(jsonPath("$.data.thumbnail").value(false));

        then(campingImageService).should()
                .addCampingImage(eq(USER_ID), eq(CAMPING_ID), any(MultipartFile.class), eq(false));
    }

    @Test
    @DisplayName("캠핑장 썸네일 이미지 등록 성공")
    void addCampingImage_thumbnail_success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "thumbnail-image-content".getBytes()
        );

        HostCampingImageResponse response = new HostCampingImageResponse(
                IMAGE_ID,
                "https://image.com/thumbnail.jpg",
                true
        );

        given(campingImageService.addCampingImage(eq(USER_ID), eq(CAMPING_ID), any(MultipartFile.class), eq(true)))
                .willReturn(response);

        mockMvc.perform(multipart("/api/host/campings/{campingId}/images", CAMPING_ID)
                        .file(image)
                        .with(hostUser)
                        .param("thumbnail", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 이미지가 등록되었습니다."))
                .andExpect(jsonPath("$.data.thumbnail").value(true));

        then(campingImageService).should()
                .addCampingImage(eq(USER_ID), eq(CAMPING_ID), any(MultipartFile.class), eq(true));
    }

    @Test
    @DisplayName("캠핑장 이미지 등록 실패 - 잘못된 campingId")
    void addCampingImage_fail_invalidCampingId() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/host/campings/{campingId}/images", 0L)
                        .file(image)
                        .with(hostUser)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest());

        then(campingImageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("캠핑장 이미지 삭제 성공")
    void deleteCampingImage_success() throws Exception {
        willDoNothing()
                .given(campingImageService)
                .deleteCampingImage(eq(USER_ID), eq(CAMPING_ID), eq(IMAGE_ID));

        mockMvc.perform(delete("/api/host/campings/{campingId}/images/{imageId}", CAMPING_ID, IMAGE_ID)
                        .with(hostUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 이미지가 삭제되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(campingImageService).should()
                .deleteCampingImage(eq(USER_ID), eq(CAMPING_ID), eq(IMAGE_ID));
    }

    @Test
    @DisplayName("캠핑장 이미지 삭제 실패 - 잘못된 imageId")
    void deleteCampingImage_fail_invalidImageId() throws Exception {
        mockMvc.perform(delete("/api/host/campings/{campingId}/images/{imageId}", CAMPING_ID, 0L)
                        .with(hostUser))
                .andExpect(status().isBadRequest());

        then(campingImageService).shouldHaveNoInteractions();
    }
}