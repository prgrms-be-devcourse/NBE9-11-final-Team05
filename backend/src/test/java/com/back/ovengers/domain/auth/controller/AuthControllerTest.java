package com.back.ovengers.domain.auth.controller;

import com.back.ovengers.domain.auth.dto.LoginRequest;
import com.back.ovengers.domain.auth.dto.SignUpRequest;
import com.back.ovengers.domain.auth.entity.RefreshToken;
import com.back.ovengers.domain.auth.repository.RefreshTokenRepository;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtProvider jwtProvider;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    User user;
    Cookie accessTokenCookie;
    Cookie refreshTokenCookie;

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

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        accessTokenCookie = new Cookie("accessToken", accessToken);
        refreshTokenCookie = new Cookie("refreshToken", refreshToken);

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());
    }

    // ===================== 회원가입 =====================

    @Test
    @DisplayName("회원가입 성공")
    void t1() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createSignUpRequest("new@test.com", "닉네임")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.email").value("new@test.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void t2() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createSignUpRequest("test@test.com", "다른닉네임")
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void t3() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createSignUpRequest("other@test.com", "길동")
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("DUPLICATE_NICKNAME"));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 8자 미만")
    void t4() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "new@test.com",
                                    "password": "1234",
                                    "name": "홍길동",
                                    "nickname": "새닉네임",
                                    "phone": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ===================== 호스트 회원가입 =====================

    @Test
    @DisplayName("호스트 회원가입 성공")
    void t5() throws Exception {
        mockMvc.perform(post("/api/auth/signup/host")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "host@test.com",
                                    "password": "password123!",
                                    "name": "호스트",
                                    "nickname": "호스트닉네임",
                                    "phone": "010-1234-5678",
                                    "businessNum": "123-45-67890",
                                    "campingName": "별빛 캠핑장",
                                    "address": "강원도 춘천시 동내면 123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("호스트 회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.role").value("HOST"));
    }

    @Test
    @DisplayName("호스트 회원가입 실패 - 이메일 중복")
    void t6() throws Exception {
        mockMvc.perform(post("/api/auth/signup/host")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "password": "password123!",
                                    "name": "호스트",
                                    "nickname": "호스트닉네임",
                                    "phone": "010-1234-5678",
                                    "businessNum": "123-45-67890",
                                    "campingName": "별빛 캠핑장",
                                    "address": "강원도 춘천시 동내면 123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("DUPLICATE_EMAIL"));
    }

    // ===================== 로그인 =====================

    @Test
    @DisplayName("로그인 성공")
    void t7() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("test@test.com", "password123!")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 없음")
    void t8() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("wrong@test.com", "password123!")
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("INVALID_LOGIN_CREDENTIALS"));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void t9() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("test@test.com", "wrongpassword!")
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("INVALID_LOGIN_CREDENTIALS"));
    }

    @Test
    @DisplayName("로그인 실패 - 정지된 계정")
    void t10() throws Exception {
        user = userRepository.save(User.builder()
                .email("banned@test.com")
                .password(passwordEncoder.encode("password123!"))
                .name("정지유저")
                .nickname("정지닉네임")
                .phone("010-0000-0000")
                .role(Role.USER)
                .status(Status.BANNED)
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("banned@test.com", "password123!")
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("BANNED_USER"));
    }

    // ===================== 토큰 재발급 =====================

    @Test
    @DisplayName("토큰 재발급 성공")
    void t11() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰이 재발급되었습니다."))
                .andExpect(cookie().exists("accessToken"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Refresh Token 없음")
    void t12() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("REFRESH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Refresh Token 불일치")
    void t13() throws Exception {
        Cookie wrongRefreshToken = new Cookie("refreshToken", "wrong.token.value");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(wrongRefreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("REFRESH_TOKEN_INVALID"));
    }

    // ===================== 로그아웃 =====================

    @Test
    @DisplayName("로그아웃 성공")
    void t14() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(accessTokenCookie)
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));

        assertThat(refreshTokenRepository.findByUserId(user.getId())).isEmpty();
    }

    // ===================== 헬퍼 메서드 =====================

    private SignUpRequest createSignUpRequest(String email, String nickname) {
        // SignUpRequest가 @Getter + 필드 방식이므로 ObjectMapper로 역직렬화
        return objectMapper.convertValue(
                java.util.Map.of(
                        "email", email,
                        "password", "password123!",
                        "name", "홍길동",
                        "nickname", nickname,
                        "phone", "010-1234-5678"
                ),
                SignUpRequest.class
        );
    }
}
