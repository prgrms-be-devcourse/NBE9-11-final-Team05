package com.back.ovengers.domain.auth.service;

import com.back.ovengers.domain.auth.entity.RefreshToken;
import com.back.ovengers.domain.auth.repository.RefreshTokenRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Refresh Token 저장
     * 이미 존재하면 업데이트, 없으면 새로 생성 (로그인할 때마다 갱신)
     */
    public void save(Long userId, String refreshToken) {

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshExpiration / 1000);

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(refreshToken)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );
    }

    /**
     * Refresh Token 검증
     * 1. DB에 존재하는지 확인
     * 2. 요청 토큰과 저장된 토큰 일치 여부 확인
     * 3. 만료 시간 확인
     */
    @Transactional(readOnly = true)
    public void validate(Long userId, String refreshToken) {

        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_MISSING));

        if (!stored.getToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        if (stored.isExpired()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * Refresh Token 삭제 (로그아웃 시 사용)
     */
    @Transactional
    public void delete(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
