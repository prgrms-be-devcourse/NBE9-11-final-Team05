package com.back.ovengers.global.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private final Key key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }


    /**
     * JWT 서명에 사용할 Key 객체 생성
     * - secret 문자열을 바이트 배열로 변환 후 HMAC-SHA 키로 변환
     * - jjwt 라이브러리가 내부적으로 키 길이에 따라 HS256/384/512 중 알맞은 알고리즘 자동 선택
     * - 주의: HS256 사용 시 secret은 최소 256bit(32byte) 이상이어야 함 (짧으면 예외 발생)
     */
    private Key getSigningKey() {
        return this.key;
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
}
