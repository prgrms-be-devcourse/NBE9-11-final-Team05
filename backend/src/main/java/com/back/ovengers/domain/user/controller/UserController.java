package com.back.ovengers.domain.user.controller;

import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.domain.user.dto.*;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.service.UserService;
import com.back.ovengers.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 1. Import 추가됨
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal User user
    ) {
        MyPageResponse response = userService.getMyPage(user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "회원 정보 조회가 완료되었습니다.",
                        response
                )
        );
    }

    @Operation(summary = "내 정보 수정")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateProfile(user.getId(), request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "회원 정보 수정이 완료되었습니다.",
                        response
                )
        );
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid DeleteAccountRequest request,
            HttpServletResponse response
    ) {
        userService.deleteAccount(user.getId(), request, response);

        return ResponseEntity.ok(new ApiResponse<>("회원탈퇴가 완료되었습니다."));
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        userService.changePassword(user.getId(), request);

        return ResponseEntity.ok(
                new ApiResponse<>("비밀번호가 변경되었습니다.")
        );
    }
}