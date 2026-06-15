package com.back.ovengers.domain.user.service;

import com.back.ovengers.domain.user.dto.MyPageResponse;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        // 리뷰와 예약 정보는 추후 추가
//        List<MyReservationResponse> reservations =
//                reservationRepository.findAllByUserId(userId)
//                        .stream()
//                        .map(reservation -> MyReservationResponse.builder()
//                                .id(reservation.getId())
//                                .rsvNum(reservation.getRsvNum())
//                                .rsvName(reservation.getRsvName())
//                                .rsvPhone(reservation.getRsvPhone())
//                                .guestCount(reservation.getGuestCount())
//                                .request(reservation.getRequest())
//                                .campingName(
//                                        reservation.getCampingSite()
//                                                .getCamping()
//                                                .getName()
//                                )
//                                .siteName(
//                                        reservation.getCampingSite()
//                                                .getName()
//                                )
//                                .address(
//                                        reservation.getCampingSite()
//                                                .getCamping()
//                                                .getAddress()
//                                )
//                                .imageUrl(
//                                        reservation.getCampingSite()
//                                                .getCamping()
//                                                .getImageUrl()
//                                )
//                                .checkIn(reservation.getCheckIn())
//                                .checkOut(reservation.getCheckOut())
//                                .build())
//                        .toList();
//
//        List<MyReviewResponse> reviews =
//                reviewRepository.findAllByUserId(userId)
//                        .stream()
//                        .map(review -> MyReviewResponse.builder()
//                                .id(review.getId())
//                                .campingName(
//                                        review.getCamping().getName()
//                                )
//                                .rating(review.getRating())
//                                .content(review.getContent())
//                                .createdAt(review.getCreatedAt())
//                                .build())
//                        .toList();

        return MyPageResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl())
    // 리뷰와 예약 정보는 추후 추가
//                .reservations(reservations)
//                .reviews(reviews)
                .build();
    }
}
