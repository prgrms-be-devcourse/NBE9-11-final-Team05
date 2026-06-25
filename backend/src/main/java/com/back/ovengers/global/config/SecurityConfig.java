package com.back.ovengers.global.config;


import com.back.ovengers.global.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .exceptionHandling(exception -> exception

                        // 인증 실패(미로그인, 토큰 없음 등) → 401 Unauthorized
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"message\":\"ACCESS_TOKEN_MISSING\",\"data\":\"Access Token이 없습니다.\"}"
                            );
                        })

                        // 인가 실패(권한 부족) → 403 Forbidden
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"message\":\"FORBIDDEN\",\"data\":\"접근 권한이 없습니다.\"}"
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/prometheus")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/me/reviews"  // 내 리뷰 목록 조회 - 인증 필요
                        ).authenticated()
                        .requestMatchers(
                                "/api/users/me/**"  // 내 정보 조회/수정/탈퇴 및 하위 API는 로그인 사용자만 접근 가능
                        ).authenticated()
                        .requestMatchers(
                                "/api/notifications/**" // 알람 관련 API는 로그인 사용자만 접근 가능
                        ).authenticated()
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/signup/host",
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/auth/refresh",
                                "/api/auth/check/email",
                                "/api/auth/check/nickname",
                                "/api/users/**",
                                "/api/campings/**"
                        ).permitAll()
                        .requestMatchers(
                                "/ws/**",
                                "/chat-test.html"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/campings/*/reviews"  // 리뷰 목록 조회 비인증 허용
                        ).permitAll()

                        //예약 시
                        .requestMatchers("/api/reservations/**").hasRole("USER")
                        .requestMatchers("/api/payments/**").hasRole("USER")


                        .requestMatchers("/api/timedeals/host/**").hasRole("HOST")
                        .requestMatchers(HttpMethod.GET, "/api/timedeals/**").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/host/**").hasRole("HOST")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 HTTP 메서드 목록
        configuration.setAllowedHeaders(List.of("*")); // 모든 요청 헤더 허용
        configuration.setAllowCredentials(true);  // 쿠키/인증 정보 포함 요청 허용

        // /api/** 경로에 위 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/swagger-ui/**", configuration);
        source.registerCorsConfiguration("/v3/api-docs/**", configuration);
        return source;
    }
}
