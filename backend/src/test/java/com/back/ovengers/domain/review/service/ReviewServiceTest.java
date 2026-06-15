package com.back.ovengers.domain.review.service;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.review.dto.ReviewRequest;
import com.back.ovengers.domain.review.entity.Review;
import com.back.ovengers.domain.review.repository.ReviewRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceTest {

    @Autowired
    ReviewService reviewService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CampingRepository campingRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReviewRepository reviewRepository;

    User user;
    Camping camping;
    Site site;
    Reservation reservation;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("test@test.com")
                .password("1234")
                .name("홍길동")
                .nickname("길동")
                .phone("010-1234-5678")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        camping = campingRepository.save(Camping.builder()
                .name("강릉 솔밭 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.APPROVED)
                .build());

        site = siteRepository.save(Site.builder()
                .camping(camping)
                .name("A구역")
                .baseCapacity(2)
                .maxCapacity(4)
                .totalAmount(5)
                .price(50000)
                .build());

        reservation = reservationRepository.save(Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum("RV-001")
                .rsvName("홍길동")
                .checkIn(LocalDate.of(2026, 7, 25))
                .checkOut(LocalDate.of(2026, 7, 27))
                .guestCount(4)
                .rsvPrice(100000)
                .status(ReservationStatus.COMPLETED)
                .build());
    }

    @Test
    void 이용완료된_예약에_리뷰를_작성할_수_있다() {
        ReviewRequest request = ReviewRequest.of(5, "좋았어요");

        reviewService.createReview(user, reservation.getId(), request);

        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getRating()).isEqualTo(5);
        assertThat(reviews.get(0).getContent()).isEqualTo("좋았어요");
    }

    @Test
    void 이용완료되지_않은_예약은_리뷰를_작성할_수_없다() {
        Reservation notCompleted = reservationRepository.save(Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum("RV-002")
                .rsvName("홍길동")
                .checkIn(LocalDate.of(2026, 8, 1))
                .checkOut(LocalDate.of(2026, 8, 3))
                .guestCount(4)
                .rsvPrice(100000)
                .status(ReservationStatus.CONFIRMED)
                .build());

        ReviewRequest request = ReviewRequest.of(5, "좋았어요");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.createReview(user, notCompleted.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_NOT_COMPLETED);
    }

    @Test
    void 본인_예약이_아니면_리뷰를_작성할_수_없다() {
        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .nickname("other")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        ReviewRequest request = ReviewRequest.of(5, "좋았어요");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.createReview(other, reservation.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ACCESS_DENIED);
    }

    @Test
    void 같은_예약에_리뷰를_중복_작성할_수_없다() {
        ReviewRequest request = ReviewRequest.of(5, "좋았어요");
        reviewService.createReview(user, reservation.getId(), request);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.createReview(user, reservation.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_REVIEWED);
    }
}
