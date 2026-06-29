package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CampingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CampingRepository campingRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    ReservationRepository reservationRepository;

    User host;
    Camping camping;
    Site site;

    @BeforeEach
    void setUp() {
        host = userRepository.save(User.builder()
                .email("host@test.com")
                .password("1234")
                .name("호스트")
                .nickname("host")
                .phone("010-1111-2222")
                .role(Role.HOST)
                .status(Status.ACTIVE)
                .build());

        camping = campingRepository.save(Camping.builder()
                .host(host)
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
    }

    @Test
    @DisplayName("캠핑장 검색 - 조건 없이 전체 조회")
    void t1() throws Exception {
        mockMvc.perform(get("/api/campings/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 검색 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("강릉 솔밭 캠핑장"));
    }

    @Test
    @DisplayName("캠핑장 검색 - 키워드 검색")
    void t2() throws Exception {
        mockMvc.perform(get("/api/campings/search")
                        .param("keyword", "강릉"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("강릉 솔밭 캠핑장"));
    }

    @Test
    @DisplayName("캠핑장 검색 - 날짜, 인원, 객실 조건")
    void t3() throws Exception {
        mockMvc.perform(get("/api/campings/search")
                        .param("checkIn", "2026-07-01")
                        .param("checkOut", "2026-07-03")
                        .param("guestCount", "2")
                        .param("roomCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("강릉 솔밭 캠핑장"));
    }

    @Test
    @DisplayName("캠핑장 검색 - 가격 범위 조건")
    void t4() throws Exception {
        mockMvc.perform(get("/api/campings/search")
                        .param("minPrice", "30000")
                        .param("maxPrice", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("강릉 솔밭 캠핑장"));
    }

    @Test
    @DisplayName("캠핑장 검색 - 조건에 맞는 캠핑장 없으면 빈 페이지 반환")
    void t5() throws Exception {
        mockMvc.perform(get("/api/campings/search")
                        .param("keyword", "존재하지않는캠핑장"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("캠핑장 검색 - 인원 초과 시 조회 안됨")
    void t6() throws Exception {
        mockMvc.perform(get("/api/campings/search")
                        .param("guestCount", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("캠핑장 검색 - 예약이 꽉 차면 조회 안됨")
    void t7() throws Exception {
        // given - site totalAmount(5)만큼 예약을 꽉 채움
        for (int i = 0; i < 5; i++) {
            reservationRepository.save(Reservation.builder()
                    .user(host)
                    .site(site)
                    .rsvNum("RV-00" + i)
                    .rsvName("테스트유저" + i)
                    .checkIn(LocalDate.of(2026, 7, 1))
                    .checkOut(LocalDate.of(2026, 7, 3))
                    .guestCount(2)
                    .rsvPrice(100000)
                    .status(ReservationStatus.CONFIRMED)
                    .build());
        }

        // when & then - 해당 날짜에 예약 가능한 캠핑장 없음
        mockMvc.perform(get("/api/campings/search")
                        .param("checkIn", "2026-07-01")
                        .param("checkOut", "2026-07-03")
                        .param("roomCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }
}

