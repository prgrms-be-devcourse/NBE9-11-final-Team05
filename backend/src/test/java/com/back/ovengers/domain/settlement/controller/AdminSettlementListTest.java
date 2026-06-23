package com.back.ovengers.domain.settlement.controller;

import com.back.ovengers.domain.settlement.entity.Settlement;
import com.back.ovengers.domain.settlement.entity.SettlementStatus;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
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
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminSettlementListTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;

    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() {
        settlementRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(User.builder()
                .email("admin" + System.nanoTime() + "@test.com")
                .password("password123!")
                .name("관리자")
                .nickname("관리자" + System.nanoTime())
                .phone("010-0000-0000")
                .role(Role.ADMIN)
                .status(com.back.ovengers.domain.user.entity.Status.ACTIVE)
                .build());

        User host = userRepository.save(
                UserFixture.host()
                        .email("host" + System.nanoTime() + "@test.com")
                        .nickname("호스트" + System.nanoTime())
                        .build()
        );

        for (int i = 0; i < 15; i++) {
            settlementRepository.save(Settlement.builder()
                    .host(host)
                    .settlementDate(LocalDate.of(2026, 6, 21).minusWeeks(i))
                    .totalAmount(1000000)
                    .feeAmount(100000)
                    .payoutAmount(900000)
                    .status(i % 2 == 0 ? SettlementStatus.PENDING : SettlementStatus.COMPLETED)
                    .build());
        }

        adminToken = jwtProvider.createRefreshToken(admin.getId(), admin.getRole().name());
    }

    @AfterEach
    void tearDown() {
        settlementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("관리자 정산 목록 조회 성공 - 첫 페이지")
    void getAllSettlements_success_firstPage() throws Exception {

        mockMvc.perform(get("/api/admin/settlements")
                        .param("page", "0")
                        .cookie(new MockCookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정산 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.content.length()").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    @DisplayName("관리자 정산 목록 조회 성공 - 마지막 페이지")
    void getAllSettlements_success_lastPage() throws Exception {

        mockMvc.perform(get("/api/admin/settlements")
                        .param("page", "1")
                        .cookie(new MockCookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("관리자 정산 목록 조회 실패 - 관리자 아닌 경우")
    void getAllSettlements_fail_forbidden() throws Exception {

        User host = userRepository.save(
                UserFixture.host()
                        .email("host2" + System.nanoTime() + "@test.com")
                        .nickname("호스트2" + System.nanoTime())
                        .build()
        );

        String hostToken = jwtProvider.createRefreshToken(host.getId(), host.getRole().name());

        mockMvc.perform(get("/api/admin/settlements")
                        .param("page", "0")
                        .cookie(new MockCookie("accessToken", hostToken)))
                .andExpect(status().isForbidden());
    }
}