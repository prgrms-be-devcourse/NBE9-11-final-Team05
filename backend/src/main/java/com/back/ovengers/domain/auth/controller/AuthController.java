package com.back.ovengers.domain.auth.controller;

import com.back.ovengers.domain.auth.dto.*;
import com.back.ovengers.domain.auth.service.AuthService;
import com.back.ovengers.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "일반 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signup(
            @Valid @RequestBody SignUpRequest request
    ) {

        SignUpResponse response = authService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "회원가입이 완료되었습니다.",
                                response
                        )
                );
    }

    @Operation(summary = "호스트 회원가입")
    @PostMapping("/signup/host")
    public ResponseEntity<ApiResponse<SignUpResponse>> hostSignup(
            @Valid @RequestBody HostSignUpRequest request
    ) {
        SignUpResponse response = authService.hostSignUp(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("호스트 회원가입이 완료되었습니다.", response));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        LoginResponse loginResponse = authService.login(request, response);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "로그인이 완료되었습니다.",
                        loginResponse
                )
        );
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);

        return ResponseEntity.ok(new ApiResponse<>("로그아웃이 완료되었습니다."));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.reissue(request, response);

        return ResponseEntity.ok(new ApiResponse<>("토큰이 재발급되었습니다."));
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check/email")
    public ResponseEntity<ApiResponse<Void>>  checkEmail(
            @Parameter(description = "확인할 이메일", example = "test@test.com")
            @RequestParam String email
    ){
        authService.checkEmail(email);

        return ResponseEntity.ok(new ApiResponse<>("사용 가능한 이메일입니다"));
    }

    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("/check/nickname")
    public ResponseEntity<ApiResponse<Void>>  checkNickname(
            @Parameter(description = "확인할 닉네임", example = "홍길동")
            @RequestParam String nickname
    ){
        authService.checkNickname(nickname);

        return ResponseEntity.ok(new ApiResponse<>("사용 가능한 닉네임입니다"));
    }
}