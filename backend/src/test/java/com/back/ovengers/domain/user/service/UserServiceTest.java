package com.back.ovengers.domain.user.service;

import com.back.ovengers.domain.auth.entity.RefreshToken;
import com.back.ovengers.domain.auth.repository.RefreshTokenRepository;
import com.back.ovengers.domain.user.dto.DeleteAccountRequest;
import com.back.ovengers.domain.user.dto.MyPageResponse;
import com.back.ovengers.domain.user.dto.UserUpdateRequest;
import com.back.ovengers.domain.user.dto.UserUpdateResponse;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    User user;
    HttpServletResponse mockResponse;

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
    }

    // ===================== 회원 정보 조회 =====================

    @Test
    @DisplayName("회원 정보 조회 성공")
    void t1() {
        MyPageResponse response = userService.getMyPage(user.getId());

        assertThat(response.getNickname()).isEqualTo("길동");
        assertThat(response.getPhone()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 유저")
    void t2() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> userService.getMyPage(999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ===================== 회원 정보 수정 =====================

    @Test
    @DisplayName("회원 정보 수정 성공 - 닉네임 변경")
    void t3() {
        UserUpdateRequest request = createUpdateRequest("새닉네임", null, null);

        UserUpdateResponse response = userService.updateProfile(user.getId(), request);

        assertThat(response.nickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 전화번호 변경")
    void t4() {
        UserUpdateRequest request = createUpdateRequest(null, "010-9999-9999", null);

        UserUpdateResponse response = userService.updateProfile(user.getId(), request);

        assertThat(response.phone()).isEqualTo("010-9999-9999");
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이미지 변경")
    void t5() {
        UserUpdateRequest request = createUpdateRequest(null, null, "https://image.url/new.jpg");

        UserUpdateResponse response = userService.updateProfile(user.getId(), request);

        assertThat(response.imageUrl()).isEqualTo("https://image.url/new.jpg");
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 닉네임 중복")
    void t6() {
        userRepository.save(User.builder()
                .email("other@test.com")
                .password(passwordEncoder.encode("password123!"))
                .name("다른유저")
                .nickname("중복닉네임")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        UserUpdateRequest request = createUpdateRequest("중복닉네임", null, null);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> userService.updateProfile(user.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 본인 닉네임과 동일한 닉네임으로 수정")
    void t7() {
        UserUpdateRequest request = createUpdateRequest("길동", null, null);

        UserUpdateResponse response = userService.updateProfile(user.getId(), request);

        assertThat(response.nickname()).isEqualTo("길동");
    }

    // ===================== 회원 탈퇴 =====================

    @Test
    @DisplayName("회원 탈퇴 성공 - deletedAt 설정 확인")
    void t8() {
        DeleteAccountRequest request = createDeleteRequest("password123!");

        userService.deleteAccount(user.getId(), request, mockResponse);

        userRepository.flush();
        User deleted = userRepository.findById(user.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - Refresh Token DB 삭제 확인")
    void t9() {
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token("someRefreshToken")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        DeleteAccountRequest request = createDeleteRequest("password123!");

        userService.deleteAccount(user.getId(), request, mockResponse);

        assertThat(refreshTokenRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
    void t10() {
        DeleteAccountRequest request = createDeleteRequest("wrongpassword!");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> userService.deleteAccount(user.getId(), request, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 유저")
    void t11() {
        DeleteAccountRequest request = createDeleteRequest("password123!");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> userService.deleteAccount(999L, request, mockResponse)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ===================== 헬퍼 메서드 =====================

    private UserUpdateRequest createUpdateRequest(
            String nickname,
            String phone,
            String imageUrl
    ) {
        try {
            UserUpdateRequest request = new UserUpdateRequest();
            if (nickname != null) setField(request, "nickname", nickname);
            if (phone != null) setField(request, "phone", phone);
            if (imageUrl != null) setField(request, "imageUrl", imageUrl);
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DeleteAccountRequest createDeleteRequest(String password) {
        try {
            DeleteAccountRequest request = new DeleteAccountRequest();
            setField(request, "password", password);
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String fieldName, String value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}