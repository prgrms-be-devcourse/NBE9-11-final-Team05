package com.back.ovengers.domain.user.service;

import com.back.ovengers.domain.auth.service.RefreshTokenService;
import com.back.ovengers.domain.reservation.dto.MyReservationResponse;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.review.dto.MyReviewResponse;
import com.back.ovengers.domain.review.repository.ReviewRepository;
import com.back.ovengers.domain.user.dto.*;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<MyReservationResponse> reservations =
                reservationRepository.findAllByUserId(userId)
                        .stream()
                        .map(reservation -> new MyReservationResponse(
                                reservation.getId(),
                                reservation.getRsvNum(),
                                reservation.getRsvName(),
                                reservation.getRsvPhone(),
                                reservation.getGuestCount(),
                                reservation.getRequest(),
                                reservation.getSite().getCamping().getName(),
                                reservation.getSite().getName(),
                                reservation.getSite().getCamping().getAddress(),
                                reservation.getSite().getCamping().getFirstImageUrl(),
                                reservation.getCheckIn(),
                                reservation.getCheckOut()
                        ))
                        .toList();

        List<MyReviewResponse> reviews =
                reviewRepository.findAllByUserId(userId)
                        .stream()
                        .map(review -> MyReviewResponse.from(
                                review.getReservation(), review
                        ))
                        .toList();

        return MyPageResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl())
                .reservations(reservations)
                .reviews(reviews)
                .role(user.getRole())
                .build();
    }


    @Transactional
    public UserUpdateResponse updateProfile(Long userId, UserUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null) {

            if (!StringUtils.hasText(request.getNickname())) {
                throw new CustomException(ErrorCode.EMPTY_NICKNAME);
            }

            if (!user.getNickname().equals(request.getNickname())
                    && userRepository.existsByNickname(request.getNickname())) {

                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }

            user.changeNickname(request.getNickname());
        }

        if (request.getPhone() != null) {

            if (!StringUtils.hasText(request.getPhone())) {
                throw new CustomException(ErrorCode.EMPTY_PHONE);
            }

            user.changePhone(request.getPhone());
        }

        if (request.getImageUrl() != null) {

            if (!StringUtils.hasText(request.getImageUrl())) {
                throw new CustomException(ErrorCode.EMPTY_IMAGE_URL);
            }

            user.changeImageUrl(request.getImageUrl());
        }

        return new UserUpdateResponse(
                user.getId(),
                user.getName(),
                user.getNickname(),
                user.getPhone(),
                user.getImageUrl(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request, HttpServletResponse response) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 입력한 비밀번호와 저장된 암호화 비밀번호 비교
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        // soft delete 처리
        user.delete();

        // DB에서 Refresh Token 삭제
        refreshTokenService.delete(userId);

        // 쿠키 삭제
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        // 기존 비밀번호와 동일한지 확인
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new CustomException(ErrorCode.SAME_PASSWORD);
        }

        // 새 비밀번호 암호화 후 저장
        user.changePassword(
                passwordEncoder.encode(request.getNewPassword())
        );
    }
}
