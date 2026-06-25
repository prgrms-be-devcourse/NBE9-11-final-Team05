package com.back.ovengers.domain.settlement.controller;

import com.back.ovengers.domain.settlement.entity.Settlement;
import com.back.ovengers.domain.settlement.entity.SettlementStatus;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminSettlementCompleteTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;

    private User admin;
    private Settlement settlement;
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
                .status(Status.ACTIVE)
                .build());

        User host = userRepository.save(
                UserFixture.host()
                        .email("host" + System.nanoTime() + "@test.com")
                        .nickname("호스트" + System.nanoTime())
                        .build()
        );

        settlement = settlementRepository.save(Settlement.builder()
                .host(host)
                .settlementDate(LocalDate.of(2026, 6, 21))
                .totalAmount(1000000)
                .feeAmount(100000)
                .payoutAmount(900000)
                .status(SettlementStatus.PENDING)
                .build());

        adminToken = jwtProvider.createRefreshToken(admin.getId(), admin.getRole().name());
    }

    @AfterEach
    void tearDown() {
        settlementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("정산 완료 처리 성공")
    void completeSettlement_success() throws Exception {

        mockMvc.perform(patch("/api/admin/settlements/{id}/complete", settlement.getId())
                        .cookie(new MockCookie("accessToken", adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정산이 완료 처리되었습니다."))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.payoutAmount").value(900000));
    }

    @Test
    @DisplayName("정산 완료 처리 실패 - 정산 없음")
    void completeSettlement_fail_notFound() throws Exception {

        mockMvc.perform(patch("/api/admin/settlements/{id}/complete", 999999L)
                        .cookie(new MockCookie("accessToken", adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value("정산 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("정산 완료 처리 실패 - 이미 완료된 정산")
    void completeSettlement_fail_alreadyCompleted() throws Exception {

        settlement.complete();
        settlementRepository.save(settlement);

        mockMvc.perform(patch("/api/admin/settlements/{id}/complete", settlement.getId())
                        .cookie(new MockCookie("accessToken", adminToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").value("이미 완료된 정산입니다."));
    }

    @Test
    @DisplayName("정산 완료 처리 실패 - 관리자 아닌 경우")
    void completeSettlement_fail_forbidden() throws Exception {

        User host = userRepository.save(
                UserFixture.host()
                        .email("host2" + System.nanoTime() + "@test.com")
                        .nickname("호스트2" + System.nanoTime())
                        .build()
        );

        String hostToken = jwtProvider.createRefreshToken(host.getId(), host.getRole().name());

        mockMvc.perform(patch("/api/admin/settlements/{id}/complete", settlement.getId())
                        .cookie(new MockCookie("accessToken", hostToken)))
                .andExpect(status().isForbidden());
    }
}