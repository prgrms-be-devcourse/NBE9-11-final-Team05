package com.back.ovengers.domain.review.controller;

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
import com.back.ovengers.global.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired JwtProvider jwtProvider;
    @Autowired UserRepository userRepository;
    @Autowired CampingRepository campingRepository;
    @Autowired SiteRepository siteRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired ReviewRepository reviewRepository;

    User user;
    Camping camping;
    Site site;
    Reservation reservation;
    Cookie accessTokenCookie;

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

        String token = jwtProvider.createAccessToken(user.getId());
        accessTokenCookie = new Cookie("accessToken", token);
    }

    // ===================== 리뷰 작성 =====================

    @Test
    @DisplayName("리뷰 작성 성공")
    void t1() throws Exception {
        ReviewRequest request = ReviewRequest.of(5, "좋았어요");

        mockMvc.perform(post("/api/reviews")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("reservationId", String.valueOf(reservation.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰가 작성되었습니다."));

        reviewRepository.flush();
        assertThat(reviewRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 미로그인")
    void t2() throws Exception {
        ReviewRequest request = ReviewRequest.of(5, "좋았어요");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("reservationId", String.valueOf(reservation.getId())))
                .andExpect(status().isForbidden());
    }

    // ===================== 캠핑장 리뷰 목록 조회 =====================

    @Test
    @DisplayName("캠핑장 리뷰 목록 조회 성공")
    void t3() throws Exception {
        reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        mockMvc.perform(get("/api/campings/{campingId}/reviews", camping.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].rating").value(5))
                .andExpect(jsonPath("$.data.content[0].content").value("좋았어요"));
    }

    @Test
    @DisplayName("캠핑장 리뷰 목록 조회 실패 - 존재하지 않는 캠핑장")
    void t4() throws Exception {
        mockMvc.perform(get("/api/campings/{campingId}/reviews", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("CAMPING_NOT_FOUND"));
    }

    // ===================== 리뷰 수정 =====================

    @Test
    @DisplayName("리뷰 수정 성공")
    void t5() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        ReviewRequest request = ReviewRequest.of(3, "그냥 그랬어요");

        mockMvc.perform(put("/api/reviews/{reviewId}", review.getId())
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰가 수정되었습니다."));

        reviewRepository.flush();
        Review updated = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(updated.getRating()).isEqualTo(3);
        assertThat(updated.getContent()).isEqualTo("그냥 그랬어요");
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 본인 리뷰 아님")
    void t6() throws Exception {
        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .nickname("other")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        Cookie otherCookie = new Cookie("accessToken", jwtProvider.createAccessToken(other.getId()));
        ReviewRequest request = ReviewRequest.of(1, "별로였어요");

        mockMvc.perform(put("/api/reviews/{reviewId}", review.getId())
                        .cookie(otherCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("REVIEW_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 미로그인")
    void t7() throws Exception {
        ReviewRequest request = ReviewRequest.of(3, "그냥 그랬어요");

        mockMvc.perform(put("/api/reviews/{reviewId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ===================== 리뷰 삭제 =====================

    @Test
    @DisplayName("리뷰 삭제 성공")
    void t8() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        mockMvc.perform(delete("/api/reviews/{reviewId}", review.getId())
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰가 삭제되었습니다."));

        reviewRepository.flush();
        assertThat(reviewRepository.findById(review.getId())).isEmpty();
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 유효하지 않은 reviewId")
    void t9() throws Exception {
        mockMvc.perform(delete("/api/reviews/{reviewId}", 0L)
                        .cookie(accessTokenCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("INVALID_PATH_VARIABLE"));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 본인 리뷰 아님")
    void t10() throws Exception {
        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .nickname("other")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        Cookie otherCookie = new Cookie("accessToken", jwtProvider.createAccessToken(other.getId()));

        mockMvc.perform(delete("/api/reviews/{reviewId}", review.getId())
                        .cookie(otherCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("REVIEW_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 미로그인")
    void t11() throws Exception {
        mockMvc.perform(delete("/api/reviews/{reviewId}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 성공")
    void t12() throws Exception {
        reviewRepository.save(Review.builder()
                .user(user)
                .camping(camping)
                .reservation(reservation)
                .rating(5)
                .content("좋았어요")
                .build());

        mockMvc.perform(get("/api/users/me/reviews")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].hasReview").value(true))
                .andExpect(jsonPath("$.data.content[0].review.rating").value(5));
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 성공 - 리뷰 없는 예약도 포함")
    void t13() throws Exception {
        mockMvc.perform(get("/api/users/me/reviews")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].hasReview").value(false))
                .andExpect(jsonPath("$.data.content[0].review").doesNotExist());
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 실패 - 미로그인")
    void t14() throws Exception {
        mockMvc.perform(get("/api/users/me/reviews"))
                .andExpect(status().isForbidden());
    }
}
