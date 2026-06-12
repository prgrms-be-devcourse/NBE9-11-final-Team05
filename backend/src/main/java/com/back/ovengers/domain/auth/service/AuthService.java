package com.back.ovengers.domain.auth.service;

import com.back.ovengers.domain.auth.dto.SignUpRequest;
import com.back.ovengers.domain.auth.dto.SignUpResponse;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
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

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {

        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNicknameAndDeletedAtIsNull(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

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

        return SignUpResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .role(savedUser.getRole().name())
                .build();
    }
}
