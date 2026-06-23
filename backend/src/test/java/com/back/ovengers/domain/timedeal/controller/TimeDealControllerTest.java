package com.back.ovengers.domain.timedeal.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.timedeal.dto.TimeDealCreateRequest;
import com.back.ovengers.domain.timedeal.dto.TimeDealUpdateRequest;
import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TimeDealControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtProvider jwtProvider;

    @Autowired UserRepository userRepository;
    @Autowired CampingRepository campingRepository;
    @Autowired SiteRepository siteRepository;
    @Autowired TimeDealRepository timeDealRepository;

    User host;
    Camping camping;
    Site site;
    Cookie accessTokenCookie;

    @BeforeEach
    void setUp() {
        host = userRepository.save(
                User.builder()
                        .email("host@test.com")
                        .password("1234")
                        .name("호스트")
                        .nickname("host")
                        .phone("010-1111-1111")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        camping = campingRepository.save(
                Camping.builder()
                        .host(host)
                        .name("강릉 캠핑장")
                        .region("강원도")
                        .city("강릉시")
                        .address("강원도 강릉시")
                        .status(CampingStatus.APPROVED)
                        .build()
        );

        site = siteRepository.save(
                Site.builder()
                        .camping(camping)
                        .name("A사이트")
                        .baseCapacity(2)
                        .maxCapacity(4)
                        .totalAmount(10)
                        .price(100000)
                        .build()
        );

        String token = jwtProvider.createAccessToken(host.getId(), host.getRole().name());
        accessTokenCookie = new Cookie("accessToken", token);
    }

    @Test
    @DisplayName("타임딜 등록 성공")
    void t1() throws Exception {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                3,
                70000,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/api/timedeals/host")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("타임딜이 등록되었습니다."));
    }

    @Test
    @DisplayName("타임딜 등록 실패 - 수량 0")
    void t2() throws Exception {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                0,
                70000,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/api/timedeals/host")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("타임딜 수정 성공")
    void t3() throws Exception {
        TimeDeal timeDeal = createTimeDeal();
        TimeDealUpdateRequest request = new TimeDealUpdateRequest(
                5,
                60000,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(patch("/api/timedeals/host/{id}", timeDeal.getId())
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("타임딜이 수정되었습니다."));
    }

    @Test
    @DisplayName("타임딜 취소 성공")
    void t4() throws Exception {
        TimeDeal timeDeal = createTimeDeal();

        mockMvc.perform(patch("/api/timedeals/host/{id}/cancel", timeDeal.getId())
                        .cookie(accessTokenCookie))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("타임딜 삭제 성공")
    void t5() throws Exception {
        TimeDeal timeDeal = createTimeDeal();

        mockMvc.perform(delete("/api/timedeals/host/{id}", timeDeal.getId())
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("타임딜이 삭제되었습니다."));
    }

    @Test
    @DisplayName("내 타임딜 목록 조회 성공")
    void t6() throws Exception {
        createTimeDeal();

        mockMvc.perform(get("/api/timedeals/host/my")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("호스트 타임딜 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].siteId").value(site.getId()));
    }

    @Test
    @DisplayName("내 타임딜 목록 조회 실패 - 미로그인")
    void t7() throws Exception {
        mockMvc.perform(get("/api/timedeals/host/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("전체 타임딜 목록 조회 성공")
    void t8() throws Exception {
        createTimeDeal();

        mockMvc.perform(get("/api/timedeals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("타임딜 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("타임딜 단건 조회 성공")
    void t9() throws Exception {
        TimeDeal timeDeal = createTimeDeal();

        mockMvc.perform(get("/api/timedeals/{id}", timeDeal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("타임딜 단건 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(timeDeal.getId()));
    }

    @Test
    @DisplayName("타임딜 단건 조회 실패 - 존재하지 않는 타임딜")
    void t10() throws Exception {
        mockMvc.perform(get("/api/timedeals/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    private TimeDeal createTimeDeal() {
        return timeDealRepository.save(
                TimeDeal.builder()
                        .site(site)
                        .checkIn(LocalDate.now().plusDays(10))
                        .checkOut(LocalDate.now().plusDays(12))
                        .quantity(3)
                        .originalPrice(site.getPrice())
                        .dealPrice(70000)

                        .saleStartAt(LocalDateTime.now().minusHours(1))
                        .saleEndAt(LocalDateTime.now().plusDays(1))
                        .build()
        );
    }
}