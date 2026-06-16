package com.back.ovengers.domain.user.controller;

import com.back.ovengers.domain.reservation.dto.ReservationResponse;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.domain.user.dto.DeleteAccountRequest;
import com.back.ovengers.domain.user.dto.MyPageResponse;
import com.back.ovengers.domain.user.dto.UserUpdateRequest;
import com.back.ovengers.domain.user.dto.UserUpdateResponse;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.service.UserService;
import com.back.ovengers.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;

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

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateProfile(
            Authentication authentication,
            @RequestBody UserUpdateRequest request
    ) {

        User user = (User) authentication.getPrincipal();

        UserUpdateResponse response = userService.updateProfile(user.getId(), request);


        return ResponseEntity.ok(
                new ApiResponse<>(
                        "회원 정보 수정이 완료되었습니다.",
                        response
                )
        );
    }

    @GetMapping("/me/reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page) {


        return ResponseEntity.ok(
                new ApiResponse<>(
                        "예약 목록 조회가 완료되었습니다.",
                        reservationService.getMyReservations(user.getId(), page)
                )
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            Authentication authentication,
            @RequestBody @Valid DeleteAccountRequest request,
            HttpServletResponse response
    ) {
        User user = (User) authentication.getPrincipal();
        userService.deleteAccount(user.getId(), request, response);

        return ResponseEntity.ok(new ApiResponse<>("회원탈퇴가 완료되었습니다."));
    }
}
