package com.back.ovengers.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${jwt.access-expiration}")
    private int accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    private static final String ACCESS_TOKEN_NAME = "accessToken";
    private static final String REFRESH_TOKEN_NAME = "refreshToken";

    /**
     * Access Token 쿠키 생성 후 응답에 추가
     */
    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {

        Cookie cookie = new Cookie(ACCESS_TOKEN_NAME, accessToken);
        cookie.setHttpOnly(true);           // JS 접근 차단 (XSS 방어)
        cookie.setSecure(cookieSecure);     // HTTPS에서만 전송 (로컬: false, 운영: true)
        cookie.setPath("/");                // 모든 경로에서 쿠키 전송
        cookie.setMaxAge(accessExpiration);    // 쿠키 만료 시간 (초 단위)
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }

    /**
     * 요청 쿠키에서 Access Token 추출
     */
    public Optional<String> getAccessToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> ACCESS_TOKEN_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Access Token 쿠키 만료 (로그아웃 시 사용)
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {

        Cookie cookie = new Cookie(ACCESS_TOKEN_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);    // 즉시 만료
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }

    /**
     * Refresh Token 쿠키 생성 후 응답에 추가
     * path를 /api/auth/reissue로 제한하여 불필요한 요청에 쿠키가 전송되지 않도록 함
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {

        Cookie cookie = new Cookie(REFRESH_TOKEN_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (refreshExpiration / 1000));
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }

    /**
     * 요청 쿠키에서 Refresh Token 추출
     */
    public Optional<String> getRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Refresh Token 쿠키 만료 (로그아웃 시 사용)
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {

        Cookie cookie = new Cookie(REFRESH_TOKEN_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }
}
