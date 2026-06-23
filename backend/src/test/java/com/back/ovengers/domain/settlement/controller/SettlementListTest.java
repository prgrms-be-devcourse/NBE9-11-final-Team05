package com.back.ovengers.domain.settlement.controller;

import com.back.ovengers.domain.settlement.entity.SettlementStatus;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.SettlementFixture;
import com.back.ovengers.fixture.UserFixture;
import com.back.ovengers.global.security.JwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class SettlementListTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;

    private User host;
    private String accessToken;

    @BeforeEach
    void setUp() {
        settlementRepository.deleteAll();
        userRepository.deleteAll();

        host = userRepository.save(
                UserFixture.host()
                        .email("host" + System.nanoTime() + "@test.com")
                        .nickname("호스트" + System.nanoTime())
                        .build()
        );

        for (int i = 0; i < 15; i++) {
            settlementRepository.save(
                    SettlementFixture.builder()
                            .host(host)
                            .settlementDate(LocalDate.of(2026, 6, 21).minusWeeks(i))
                            .status(i % 2 == 0 ? SettlementStatus.PENDING : SettlementStatus.COMPLETED)
                            .build()
            );
        }

        accessToken = jwtProvider.createRefreshToken(host.getId(), host.getRole().name());
    }

    @AfterEach
    void tearDown() {
        settlementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("정산 목록 조회 성공 - 첫 페이지")
    void getMySettlements_success_firstPage() throws Exception {

        mockMvc.perform(get("/api/host/settlements")
                        .param("page", "0")
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정산 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.content.length()").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    @DisplayName("정산 목록 조회 성공 - 마지막 페이지")
    void getMySettlements_success_lastPage() throws Exception {

        mockMvc.perform(get("/api/host/settlements")
                        .param("page", "1")
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("정산 목록 조회 성공 - 정산 없는 경우")
    void getMySettlements_success_empty() throws Exception {

        User newHost = userRepository.save(
                UserFixture.host()
                        .email("new" + System.nanoTime() + "@test.com")
                        .nickname("새호스트" + System.nanoTime())
                        .build()
        );

        String newToken = jwtProvider.createRefreshToken(newHost.getId(), newHost.getRole().name());

        mockMvc.perform(get("/api/host/settlements")
                        .param("page", "0")
                        .cookie(new MockCookie("accessToken", newToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("정산 목록 조회 실패 - 호스트가 아닌 경우")
    void getMySettlements_fail_forbidden() throws Exception {

        User user = userRepository.save(
                UserFixture.user()
                        .email("user" + System.nanoTime() + "@test.com")
                        .nickname("유저" + System.nanoTime())
                        .build()
        );

        String userToken = jwtProvider.createRefreshToken(user.getId(), user.getRole().name());

        mockMvc.perform(get("/api/host/settlements")
                        .param("page", "0")
                        .cookie(new MockCookie("accessToken", userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("정산 목록 조회 실패 - 토큰 없음")
    void getMySettlements_fail_unauthorized() throws Exception {

        mockMvc.perform(get("/api/host/settlements")
                        .param("page", "0"))
                .andExpect(status().isUnauthorized());
    }
}