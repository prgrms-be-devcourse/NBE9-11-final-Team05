package com.back.ovengers.global.security;

import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
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

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 현재 요청의 URI 경로를 가져옴
        String path = request.getRequestURI();

        // /api/auth 로 시작하는 경로(로그인, 회원가입 등)는 인증 없이 통과시킴
        // 인증 전 단계이므로 토큰이 존재하지 않는 게 정상
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 헤더에서 "Authorization" 값을 꺼냄
        String authorizationHeader = request.getHeader("Authorization");


        // Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 인증 실패 처리
        // null 체크를 먼저 해야 startsWith에서 NPE가 발생하지 않음
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

            throw new CustomException(
                    ErrorCode.ACCESS_TOKEN_MISSING
            );
        }

        // "Bearer " 이후의 실제 JWT 토큰 문자열만 추출 (앞 7글자 제거)
        String token = authorizationHeader.substring(7);

        // JwtProvider를 통해 토큰의 유효성 검사
        // 내부적으로 서명 검증 + 만료 시간 체크를 수행
        if (jwtProvider.validateToken(token)) {

            // 유효한 토큰에서 userId(PK) 클레임을 파싱해 꺼냄
            Long userId = jwtProvider.getUserId(token);

            // userId로 DB에서 실제 유저를 조회
            // 탈퇴하거나 존재하지 않는 유저의 토큰으로 접근하는 경우를 막기 위함
            User user = userRepository.findById(userId)
                            .orElseThrow(
                                    () -> new CustomException(
                                            ErrorCode.USER_NOT_FOUND
                                    )
                            );

            // soft delete 처리된 유저는 deletedAt 필드가 채워져 있음
            // 실제로 DB에 존재하더라도 탈퇴한 유저라면 접근 차단
            if (user.getDeletedAt() != null) {
                throw new CustomException(
                        ErrorCode.ALREADY_DELETED
                );
            }

            // Spring Security의 인증 객체 생성
            // 첫 번째 인자(principal): 인증된 유저 객체 → 컨트롤러에서 @AuthenticationPrincipal로 꺼낼 수 있음
            // 두 번째 인자(credentials): 비밀번호 등 자격증명 → JWT 방식에서는 불필요하므로 null
            // 세 번째 인자(authorities): 권한 목록 → 지금은 null (추후 Role 기반 접근 제어 시 추가)
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            null
                    );

            // 요청 IP, 세션 ID 등 부가적인 요청 정보를 인증 객체에 추가
            // 보안 감사(audit)나 로깅에 활용될 수 있음
            auth.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            // 완성된 인증 객체를 SecurityContext에 저장
            // 이 시점부터 해당 요청은 "인증된 사용자"로 처리됨
            // 이후 컨트롤러에서 SecurityContextHolder.getContext().getAuthentication()으로 꺼낼 수 있음
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
