package com.back.ovengers.global.security;

import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 쿠키에서 토큰 추출 — 없으면 CustomException 발생
            String token = cookieUtil.getAccessToken(request)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCESS_TOKEN_MISSING));

            // 서명 검증 + 만료 시간 체크
            // 만료 시 CustomException(ACCESS_TOKEN_EXPIRED) 발생
            if (jwtProvider.validateToken(token)) {

                Long userId = jwtProvider.getUserId(token);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (user.getDeletedAt() != null) {
                    throw new CustomException(ErrorCode.ALREADY_DELETED);
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                null
                        );

                auth.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 필터에서 발생한 모든 예외를 GlobalExceptionHandler로 위임
            // CustomException → handleCustomException()
            // 그 외 예외 → handleException()
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
