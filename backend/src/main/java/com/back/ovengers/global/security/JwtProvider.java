package com.back.ovengers.global.security;

import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    // JWT 서명/검증에 사용할 SecretKey 객체
    private final SecretKey key;

    // Access Token 만료 시간
    private final long accessExpiration;

    // Refresh Token 만료 시간
    private final long refreshExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        // secret 문자열 → UTF-8 바이트 배열 → HMAC-SHA SecretKey 객체로 변환
        // Keys.hmacShaKeyFor()는 키 길이에 따라 자동으로 HS256/384/512 결정
        // HS256 기준 최소 32바이트(256bit) 이상이어야 하며, 짧으면 WeakKeyException 발생
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }


    // 내부에서 서명 키를 꺼낼 때 사용하는 메서드
    // 생성자에서 한 번만 만들어두고 재사용하기 때문에 매번 변환 비용이 발생하지 않음
    private SecretKey getSigningKey() {
        return key;
    }



    /**
     * Access Token 생성
     * - subject(클레임의 주체)에 userId를 문자열로 저장
     * - 발급 시각(issuedAt)과 만료 시각(expiration) 설정
     * - 서명(signWith)으로 위변조 방지
     * - compact()로 최종 JWT 문자열(xxx.yyy.zzz 형태) 생성
     */
    public String createAccessToken(Long userId) {

        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))           // 토큰 주체 = 유저 ID
                .issuedAt(now)                              // 발급 시각
                .expiration(
                        new Date(
                                now.getTime() + accessExpiration  // 발급 시각 + 만료 기간(ms)
                        )
                )
                .signWith(getSigningKey())                  // 서명 (알고리즘은 키 길이에 따라 자동 결정)
                .compact();                                  // JWT 문자열로 직렬화
    }


    /**
     * Refresh Token 생성
     * - Access Token과 구조는 동일하나, 만료 기간이 더 길게 설정됨 (refreshExpiration)
     * - Access Token 재발급용으로 사용
     */
    public String createRefreshToken(Long userId) {

        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(
                        new Date(
                                now.getTime() + refreshExpiration
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    // 사용자ID 추출
    public Long getUserId(String token) {

        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }

    public boolean validateToken(String token) {

        try {

            // 서명 검증 + 만료 시간 검증을 동시에 수행
            // 문제없으면 true 반환
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (ExpiredJwtException e) {
            // 토큰 만료: 서명은 유효하지만 exp 시각이 현재 시각보다 과거인 경우
            // false 대신 예외를 던져 "만료됨"을 명확히 구분 → 클라이언트가 재발급 요청 가능
            throw new CustomException(
                    ErrorCode.ACCESS_TOKEN_EXPIRED
            );

        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: 서명 불일치, 토큰 형식 오류 등 그 외 JWT 관련 예외
            // IllegalArgumentException: token이 null이거나 빈 문자열인 경우
            // 만료와 달리 재발급 의미가 없으므로 그냥 false 반환
            return false;
        }
    }

    public void validateRefreshToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);

        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }
}
