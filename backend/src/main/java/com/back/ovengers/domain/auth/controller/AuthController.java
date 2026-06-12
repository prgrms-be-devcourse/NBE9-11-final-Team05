package com.back.ovengers.domain.auth.controller;

import com.back.ovengers.domain.auth.dto.SignUpRequest;
import com.back.ovengers.domain.auth.dto.SignUpResponse;
import com.back.ovengers.domain.auth.service.AuthService;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

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
}