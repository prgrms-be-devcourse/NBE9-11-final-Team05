package com.back.ovengers.domain.user.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtProvider jwtProvider;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    User user;
    Cookie accessTokenCookie;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("password123!"))
                .name("홍길동")
                .nickname("길동")
                .phone("010-1234-5678")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        accessTokenCookie = new Cookie("accessToken", accessToken);
    }

    // ===================== 회원 정보 조회 =====================

    @Test
    @DisplayName("회원 정보 조회 성공")
    void t1() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정보 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.nickname").value("길동"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"));
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 미로그인")
    void t2() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== 회원 정보 수정 =====================

    @Test
    @DisplayName("회원 정보 수정 성공 - 닉네임 변경")
    void t3() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nickname": "새닉네임"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정보 수정이 완료되었습니다."))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 전화번호 변경")
    void t4() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "010-9999-9999"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("010-9999-9999"));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 닉네임 중복")
    void t5() throws Exception {
        userRepository.save(User.builder()
                .email("other@test.com")
                .password(passwordEncoder.encode("password123!"))
                .name("다른유저")
                .nickname("중복닉네임")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        mockMvc.perform(patch("/api/users/me")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nickname": "중복닉네임"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("DUPLICATE_NICKNAME"));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 미로그인")
    void t6() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nickname": "새닉네임"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ===================== 회원 탈퇴 =====================

    @Test
    @DisplayName("회원 탈퇴 성공")
    void t7() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "password": "password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원탈퇴가 완료되었습니다."));

        userRepository.flush();
        User deleted = userRepository.findById(user.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
    void t8() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "password": "wrongpassword!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("INVALID_LOGIN_CREDENTIALS"));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 미로그인")
    void t9() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "password": "password123!"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}