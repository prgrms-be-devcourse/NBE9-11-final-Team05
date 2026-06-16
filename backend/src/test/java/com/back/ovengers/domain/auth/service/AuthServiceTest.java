package com.back.ovengers.domain.auth.service;

import com.back.ovengers.domain.auth.dto.LoginRequest;
import com.back.ovengers.domain.auth.dto.LoginResponse;
import com.back.ovengers.domain.auth.dto.SignUpRequest;
import com.back.ovengers.domain.auth.dto.SignUpResponse;
import com.back.ovengers.domain.auth.entity.RefreshToken;
import com.back.ovengers.domain.auth.repository.RefreshTokenRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.security.JwtProvider;
import com.back.ovengers.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;
    @Autowired CookieUtil cookieUtil;

    User user;
    HttpServletResponse mockResponse;
    HttpServletRequest mockRequest;

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

        mockResponse = mock(HttpServletResponse.class);
        mockRequest = mock(HttpServletRequest.class);
    }

    // ===================== 회원가입 =====================

    @Test
    @DisplayName("회원가입 성공")
    void t1() {
        SignUpRequest request = createSignUpRequest("new@test.com", "새닉네임");

        SignUpResponse response = authService.signUp(request);

        assertThat(response.email()).isEqualTo("new@test.com");
        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void t2() {
        SignUpRequest request = createSignUpRequest("test@test.com", "새닉네임");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.signUp(request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void t3() {
        SignUpRequest request = createSignUpRequest("new@test.com", "길동");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.signUp(request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("탈퇴한 회원의 이메일로 재가입 가능")
    void t4() {
        user.delete();
        userRepository.flush();

        SignUpRequest request = createSignUpRequest("test@test.com", "새닉네임");
        SignUpResponse response = authService.signUp(request);

        assertThat(response.email()).isEqualTo("test@test.com");
    }

    // ===================== 로그인 =====================

    @Test
    @DisplayName("로그인 성공 - Refresh Token DB 저장 확인")
    void t5() {
        LoginRequest request = new LoginRequest("test@test.com", "password123!");

        LoginResponse response = authService.login(request, mockResponse);

        assertThat(response.role()).isEqualTo("USER");
        assertThat(refreshTokenRepository.findByUserId(user.getId())).isPresent();
    }

    @Test
    @DisplayName("로그인 성공 - 재로그인 시 Refresh Token 갱신")
    void t6() throws InterruptedException{
        LoginRequest request = new LoginRequest("test@test.com", "password123!");

        authService.login(request, mockResponse);
        String firstToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow().getToken();

        Thread.sleep(1000); //1초 대기 — 같은 시각에 발급하면 토큰이 동일함

        authService.login(request, mockResponse);
        String secondToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow().getToken();

        // 재로그인 시 토큰이 갱신되어야 함
        assertThat(firstToken).isNotEqualTo(secondToken);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void t7() {
        LoginRequest request = new LoginRequest("wrong@test.com", "password123!");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.login(request, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void t8() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpassword!");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.login(request, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 정지된 계정")
    void t9() {
        userRepository.save(User.builder()
                .email("banned@test.com")
                .password(passwordEncoder.encode("password123!"))
                .name("정지유저")
                .nickname("정지닉네임")
                .phone("010-0000-0000")
                .role(Role.USER)
                .status(Status.BANNED)
                .build());

        LoginRequest request = new LoginRequest("banned@test.com", "password123!");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.login(request, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BANNED_USER);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴한 계정")
    void t10() {
        user.delete();
        userRepository.flush();

        LoginRequest request = new LoginRequest("test@test.com", "password123!");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.login(request, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }

    // ===================== 토큰 재발급 =====================

    @Test
    @DisplayName("토큰 재발급 성공")
    void t11() {
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        when(mockRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refreshToken", refreshToken)}
        );

        // 예외 없이 실행되면 성공
        authService.reissue(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Refresh Token 없음")
    void t12() {
        when(mockRequest.getCookies()).thenReturn(null);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.reissue(mockRequest, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_MISSING);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - DB 토큰과 불일치")
    void t13() {
        String savedToken = jwtProvider.createRefreshToken(user.getId());

        //같은 userId지만 DB에 저장된 토큰과 다른 값으로 조작
        String requestToken = savedToken + "tampered";

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(savedToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        when(mockRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refreshToken", requestToken)}
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.reissue(mockRequest, mockResponse)
        );

        // 서명 검증 단계에서 REFRESH_TOKEN_INVALID 발생
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Refresh Token 만료")
    void t14() {
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 이미 만료
                .build());

        when(mockRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refreshToken", refreshToken)}
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.reissue(mockRequest, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    // ===================== 로그아웃 =====================

    @Test
    @DisplayName("로그아웃 성공 - DB에서 Refresh Token 삭제")
    void t15() {
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        when(mockRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refreshToken", refreshToken)}
        );

        authService.logout(mockRequest, mockResponse);

        assertThat(refreshTokenRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("로그아웃 성공 - Refresh Token 없어도 정상 처리")
    void t16() {
        when(mockRequest.getCookies()).thenReturn(null);

        // 예외 없이 실행되면 성공
        authService.logout(mockRequest, mockResponse);
    }

    // ===================== 헬퍼 메서드 =====================

    private SignUpRequest createSignUpRequest(String email, String nickname) {
        SignUpRequest request = new SignUpRequest();
        // Reflection으로 필드 세팅 (SignUpRequest가 @Getter + 필드 방식)
        try {
            setField(request, "email", email);
            setField(request, "password", "password123!");
            setField(request, "name", "홍길동");
            setField(request, "nickname", nickname);
            setField(request, "phone", "010-1234-5678");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    private void setField(Object obj, String fieldName, String value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}