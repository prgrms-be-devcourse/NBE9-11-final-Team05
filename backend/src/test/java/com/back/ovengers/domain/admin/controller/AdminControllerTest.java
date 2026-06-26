package com.back.ovengers.domain.admin.controller;

import com.back.ovengers.domain.admin.dto.CampingBulkApproveRequest;
import com.back.ovengers.domain.admin.dto.CampingRejectRequest;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
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
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CampingRepository campingRepository;

    User adminUser;
    User normalUser;
    User hostUser;
    Cookie adminCookie;
    Cookie userCookie;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.save(User.builder()
                .email("admin@test.com")
                .password("1234")
                .name("관리자")
                .nickname("admin")
                .phone("010-0000-0000")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build());

        normalUser = userRepository.save(User.builder()
                .email("user@test.com")
                .password("1234")
                .name("일반유저")
                .nickname("user")
                .phone("010-1111-1111")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        hostUser = userRepository.save(User.builder()  // 추가
                .email("host@test.com")
                .password("1234")
                .name("호스트")
                .nickname("host")
                .phone("010-2222-2222")
                .role(Role.HOST)
                .status(Status.ACTIVE)
                .build());

        String adminToken = jwtProvider.createAccessToken(adminUser.getId(), "ADMIN");
        String userToken = jwtProvider.createAccessToken(normalUser.getId(), "USER");

        adminCookie = new Cookie("accessToken", adminToken);
        userCookie = new Cookie("accessToken", userToken);
    }

    @Test
    @DisplayName("관리자 대시보드 조회 성공")
    void t1() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관리자 대시보드 조회 성공"));
    }

    @Test
    @DisplayName("관리자 대시보드 조회 실패 - 일반 유저 접근")
    void t2() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .cookie(userCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자 대시보드 조회 실패 - 미로그인")
    void t3() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("캠핑장 승인 성공")
    void t4() throws Exception {
        Camping camping = campingRepository.save(Camping.builder()
                .host(hostUser)
                .name("승인 대기 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        mockMvc.perform(patch("/api/admin/campings/{campingId}/approve", camping.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 승인 성공"));

        Camping updated = campingRepository.findById(camping.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CampingStatus.APPROVED);
    }

    @Test
    @DisplayName("캠핑장 거절 성공")
    void t5() throws Exception {
        Camping camping = campingRepository.save(Camping.builder()
                .host(hostUser)
                .name("승인 대기 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        CampingRejectRequest request = new CampingRejectRequest("사업자 정보가 일치하지 않습니다.");

        mockMvc.perform(patch("/api/admin/campings/{campingId}/reject", camping.getId())
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 거절 성공"));

        Camping updated = campingRepository.findById(camping.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CampingStatus.REJECTED);
    }

    @Test
    @DisplayName("캠핑장 일괄 승인 성공")
    void t6() throws Exception {
        Camping camping1 = campingRepository.save(Camping.builder()
                .host(hostUser)
                .name("캠핑장1")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        Camping camping2 = campingRepository.save(Camping.builder()
                .host(hostUser)
                .name("캠핑장2")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        CampingBulkApproveRequest request = new CampingBulkApproveRequest(
                List.of(camping1.getId(), camping2.getId())
        );

        mockMvc.perform(patch("/api/admin/campings/approve")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 일괄 승인 성공"));
    }

    @Test
    @DisplayName("회원 목록 조회 성공")
    void t7() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 목록 조회 성공"));
    }

    @Test
    @DisplayName("회원 목록 조회 실패 - 일반 유저 접근")
    void t8() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .cookie(userCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 목록 조회 실패 - 미로그인")
    void t9() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 정지 성공")
    void t10() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}/ban", normalUser.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정지가 완료되었습니다."));

        User updated = userRepository.findById(normalUser.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.BANNED);
    }

    @Test
    @DisplayName("회원 정지 실패 - 일반 유저 접근")
    void t11() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}/ban", normalUser.getId())
                        .cookie(userCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 정지 해제 성공")
    void t12() throws Exception {
        // given - 먼저 정지 처리
        mockMvc.perform(patch("/api/admin/users/{userId}/ban", normalUser.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk());

        // when - 정지 해제
        mockMvc.perform(patch("/api/admin/users/{userId}/unban", normalUser.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정지 해제가 완료되었습니다."));

        // then
        User updated = userRepository.findById(normalUser.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.ACTIVE);
    }
}
