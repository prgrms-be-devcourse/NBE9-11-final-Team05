package com.back.ovengers.domain.site.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HostSiteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtProvider jwtProvider;
    @Autowired UserRepository userRepository;
    @Autowired CampingRepository campingRepository;
    @Autowired SiteRepository siteRepository;

    User host;
    User otherHost;
    Camping camping;
    Cookie hostCookie;
    Cookie otherHostCookie;

    @BeforeEach
    void setUp() {
        host = userRepository.save(User.builder()
                .email("host@test.com")
                .password("password123!")
                .name("호스트")
                .nickname("호스트닉")
                .phone("010-1111-2222")
                .role(Role.HOST)
                .status(Status.ACTIVE)
                .build());

        otherHost = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password123!")
                .name("다른호스트")
                .nickname("다른호스트닉")
                .phone("010-3333-4444")
                .role(Role.HOST)
                .status(Status.ACTIVE)
                .build());

        camping = campingRepository.save(Camping.builder()
                .host(host)
                .contentId(999L)
                .name("테스트캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 어딘가")
                .firstImageUrl("https://image.com/test.jpg")
                .rating(4.5f)
                .status(CampingStatus.APPROVED)
                .build());

        hostCookie = new Cookie("accessToken",
                jwtProvider.createAccessToken(host.getId(), host.getRole().name()));
        otherHostCookie = new Cookie("accessToken",
                jwtProvider.createAccessToken(otherHost.getId(), otherHost.getRole().name()));
    }

    @Test
    @DisplayName("구역 목록 조회 성공")
    void t1() throws Exception {
        mockMvc.perform(get("/api/host/campings/{campingId}/sites", camping.getId())
                        .cookie(hostCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("구역 목록 조회 성공"));
    }

    @Test
    @DisplayName("구역 목록 조회 실패 - 미로그인")
    void t2() throws Exception {
        mockMvc.perform(get("/api/host/campings/{campingId}/sites", camping.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("구역 목록 조회 실패 - 다른 호스트의 캠핑장")
    void t3() throws Exception {
        mockMvc.perform(get("/api/host/campings/{campingId}/sites", camping.getId())
                        .cookie(otherHostCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("구역 등록 성공")
    void t4() throws Exception {
        mockMvc.perform(post("/api/host/campings/{campingId}/sites", camping.getId())
                        .cookie(hostCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "A구역",
                                    "description": "넓은 구역",
                                    "baseCapacity": 2,
                                    "maxCapacity": 4,
                                    "totalAmount": 5,
                                    "price": 30000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("구역 등록이 완료되었습니다."))
                .andExpect(jsonPath("$.data.name").value("A구역"));
    }

    @Test
    @DisplayName("구역 등록 실패 - 유효성 검증 실패")
    void t5() throws Exception {
        mockMvc.perform(post("/api/host/campings/{campingId}/sites", camping.getId())
                        .cookie(hostCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "baseCapacity": 2,
                                    "maxCapacity": 4,
                                    "totalAmount": 5,
                                    "price": 30000
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("구역 등록 실패 - 미로그인")
    void t6() throws Exception {
        mockMvc.perform(post("/api/host/campings/{campingId}/sites", camping.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "A구역",
                                    "baseCapacity": 2,
                                    "maxCapacity": 4,
                                    "totalAmount": 5,
                                    "price": 30000
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("구역 수정 성공")
    void t7() throws Exception {
        Site site = siteRepository.save(Site.create(camping, new com.back.ovengers.domain.site.dto.SiteCreateRequest(
                "A구역", "설명", 2, 4, 5, 30000
        )));

        mockMvc.perform(patch("/api/host/campings/{campingId}/sites/{siteId}", camping.getId(), site.getId())
                        .cookie(hostCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "B구역",
                                    "price": 50000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("구역 수정이 완료되었습니다."))
                .andExpect(jsonPath("$.data.name").value("B구역"));
    }

    @Test
    @DisplayName("구역 삭제 성공")
    void t8() throws Exception {
        Site site = siteRepository.save(Site.create(camping, new com.back.ovengers.domain.site.dto.SiteCreateRequest(
                "삭제구역", "설명", 2, 4, 5, 30000
        )));

        mockMvc.perform(delete("/api/host/campings/{campingId}/sites/{siteId}", camping.getId(), site.getId())
                        .cookie(hostCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("구역 삭제가 완료되었습니다."));
    }

    @Test
    @DisplayName("구역 삭제 실패 - 다른 호스트의 캠핑장")
    void t9() throws Exception {
        Site site = siteRepository.save(Site.create(camping, new com.back.ovengers.domain.site.dto.SiteCreateRequest(
                "삭제구역", "설명", 2, 4, 5, 30000
        )));

        mockMvc.perform(delete("/api/host/campings/{campingId}/sites/{siteId}", camping.getId(), site.getId())
                        .cookie(otherHostCookie))
                .andExpect(status().isForbidden());
    }
}
