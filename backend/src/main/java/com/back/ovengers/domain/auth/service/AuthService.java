package com.back.ovengers.domain.auth.service;

import com.back.ovengers.domain.auth.dto.LoginRequest;
import com.back.ovengers.domain.auth.dto.LoginResponse;
import com.back.ovengers.domain.auth.dto.SignUpRequest;
import com.back.ovengers.domain.auth.dto.SignUpResponse;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;              // JWT 토큰 생성 담당

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {

        // 이메일 중복 체크 (탈퇴(삭제)되지 않은 유저 중에서만 검사)
        //    -> deletedAt이 null인 경우만 체크하므로, 탈퇴한 회원의 이메일은 재가입 가능
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 체크 (동일하게 삭제되지 않은 유저 기준)
        if (userRepository.existsByNicknameAndDeletedAtIsNull(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        //  User 엔티티 생성
        //    - 비밀번호는 평문 저장 금지 -> passwordEncoder로 암호화 후 저장
        //    - role은 기본값 USER, status는 기본값 ACTIVE로 설정
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);


        // 응답 DTO로 변환하여 반환 (비밀번호 등 민감 정보는 제외)
        return SignUpResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .role(savedUser.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(
            LoginRequest request
    ) {

        //이메일로 유저 조회, 없으면 예외 발생
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //탈퇴(삭제)된 계정인지 확인
        if (user.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.ALREADY_DELETED);
        }

        //정지(BANNED) 상태인 계정인지 확인
        if (user.getStatus() == Status.BANNED) {
            throw new CustomException(ErrorCode.BANNED_USER);
        }

        // 입력한 비밀번호와 저장된 암호화 비밀번호 비교
        //    matches(평문, 암호화된 비밀번호) 순서 주의
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        //인증 성공 -> Access Token 발급 (유저 ID 기반)
        String accessToken = jwtProvider.createAccessToken(user.getId());

        return new LoginResponse(
                accessToken,
                user.getRole().name()
        );
    }
}
