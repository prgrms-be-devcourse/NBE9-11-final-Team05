package com.back.ovengers.domain.user.controller;

import com.back.ovengers.domain.user.dto.MyPageResponse;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.service.UserService;
import com.back.ovengers.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        MyPageResponse response = userService.getMyPage(user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "회원 정보 조회가 완료되었습니다.",
                        response
                )
        );
    }
}
