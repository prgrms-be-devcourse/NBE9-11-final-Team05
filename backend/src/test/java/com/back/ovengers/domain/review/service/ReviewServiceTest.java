package com.back.ovengers.domain.review.service;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.review.dto.MyReviewResponse;
import com.back.ovengers.domain.review.dto.ReviewRequest;
import com.back.ovengers.domain.review.dto.ReviewResponse;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    EntityManager em;
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
    @DisplayName("이용 완료된 예약에 리뷰를 작성할 수 있다")
    void test1() {
        ReviewRequest request = ReviewRequest.of(5, "좋았어요");

        reviewService.createReview(user, reservation.getId(), request);

        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getRating()).isEqualTo(5);
        assertThat(reviews.get(0).getContent()).isEqualTo("좋았어요");
    }

    @Test
    @DisplayName("이용 완료되지 않은 예약은 리뷰를 작성할 수 없다")
    void test2() {
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
    @DisplayName("본인 예약이 아니면 리뷰를 작성할 수 없다")
    void test3() {
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
    @DisplayName("같은 예약에 리뷰를 중복 작성할 수 없다")
    void test4() {
        ReviewRequest request = ReviewRequest.of(5, "좋았어요");
        reviewService.createReview(user, reservation.getId(), request);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.createReview(user, reservation.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_REVIEWED);
    }

    @Test
    @DisplayName("캠핑장 리뷰 목록을 조회할 수 있다")
    void test5() {
        reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ReviewResponse> result = reviewService.getCampingReviews(camping.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).rating()).isEqualTo(5);
        assertThat(result.getContent().get(0).content()).isEqualTo("좋았어요");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 캠핑장 리뷰 조회 시 예외가 발생한다")
    void test6() {
        Pageable pageable = PageRequest.of(0, 10);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.getCampingReviews(999L, pageable)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAMPING_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰를 수정할 수 있다")
    void test7() {
        // given
        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        ReviewRequest request = ReviewRequest.of(3, "그냥 그랬어요");

        // when
        reviewService.updateReview(user, review.getId(), request);

        // then
        Review updated = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(updated.getRating()).isEqualTo(3);
        assertThat(updated.getContent()).isEqualTo("그냥 그랬어요");
    }

    @Test
    @DisplayName("본인 리뷰가 아니면 수정할 수 없다")
    void test8() {
        // given
        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .nickname("other")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        ReviewRequest request = ReviewRequest.of(1, "별로였어요");

        // when & then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.updateReview(other, review.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 수정 시 예외가 발생한다")
    void test9() {
        ReviewRequest request = ReviewRequest.of(3, "그냥 그랬어요");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.updateReview(user, 999L, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰를 삭제할 수 있다")
    void test10() {
        // given
        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        // when
        reviewService.deleteReview(user, review.getId());

        // then
        assertThat(reviewRepository.findById(review.getId())).isEmpty();
    }

    @Test
    @DisplayName("본인 리뷰가 아니면 삭제할 수 없다")
    void test11() {
        // given
        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .nickname("other")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        // when & then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.deleteReview(other, review.getId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 삭제 시 예외가 발생한다")
    void test12() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> reviewService.deleteReview(user, 999L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("내 리뷰 목록을 조회할 수 있다")
    void test13() {
        // given
        reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<MyReviewResponse> result = reviewService.getMyReviews(user, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).hasReview()).isTrue();
        assertThat(result.getContent().get(0).review().rating()).isEqualTo(5);
        assertThat(result.getContent().get(0).review().content()).isEqualTo("좋았어요");
    }

    @Test
    @DisplayName("리뷰가 없는 예약도 목록에 포함된다")
    void test14() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<MyReviewResponse> result = reviewService.getMyReviews(user, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).hasReview()).isFalse();
        assertThat(result.getContent().get(0).review()).isNull();
    }

    @Test
    @DisplayName("완료되지 않은 예약은 목록에 포함되지 않는다")
    void test15() {
        // given
        reservationRepository.save(Reservation.builder()
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

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<MyReviewResponse> result = reviewService.getMyReviews(user, pageable);

        // then
        assertThat(result.getContent()).hasSize(1); // COMPLETED만 조회
    }
}