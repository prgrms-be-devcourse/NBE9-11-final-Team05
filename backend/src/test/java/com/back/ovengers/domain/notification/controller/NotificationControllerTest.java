package com.back.ovengers.domain.notification.controller;

import com.back.ovengers.domain.notification.entity.Notification;
import com.back.ovengers.domain.notification.entity.NotificationType;
import com.back.ovengers.domain.notification.repository.NotificationRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtProvider jwtProvider;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;

    User user;
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

        String token = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        accessTokenCookie = new Cookie("accessToken", token);
    }

    @Test
    @DisplayName("SSE 구독 성공")
    void t0() throws Exception {
        mockMvc.perform(get("/api/notifications/subscribe")
                        .cookie(accessTokenCookie)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SSE 구독 실패 - 미로그인")
    void t0_1() throws Exception {
        mockMvc.perform(get("/api/notifications/subscribe")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("알림 목록 조회 성공")
    void t1() throws Exception {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(NotificationType.CAMPING_APPROVED)
                .content("캠핑장이 승인되었습니다.")
                .isRead(false)
                .build());

        mockMvc.perform(get("/api/notifications")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("알림 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].content").value("캠핑장이 승인되었습니다."));
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - 미로그인")
    void t2() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("안 읽은 알림 개수 조회 성공")
    void t3() throws Exception {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(NotificationType.PAYMENT_DONE)
                .content("결제가 완료되었습니다.")
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(user)
                .type(NotificationType.CAMPING_APPROVED)
                .content("캠핑장이 승인되었습니다.")
                .isRead(true)
                .build());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("안 읽은 알림 개수 조회 성공"))
                .andExpect(jsonPath("$.data.unreadCount").value(1));
    }

    @Test
    @DisplayName("안 읽은 알림 개수 조회 실패 - 미로그인")
    void t4() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("알림 단건 읽음 처리 성공")
    void t5() throws Exception {
        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .type(NotificationType.PAYMENT_DONE)
                .content("결제가 완료되었습니다.")
                .isRead(false)
                .build());

        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notification.getId())
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("알림을 읽었습니다."));
    }

    @Test
    @DisplayName("알림 단건 읽음 처리 실패 - 유효하지 않은 notificationId")
    void t6() throws Exception {
        mockMvc.perform(patch("/api/notifications/{notificationId}/read", 0L)
                        .cookie(accessTokenCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("알림 단건 읽음 처리 실패 - 본인 알림 아님")
    void t7() throws Exception {
        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .nickname("other")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        Notification notification = notificationRepository.save(Notification.builder()
                .user(other)
                .type(NotificationType.PAYMENT_DONE)
                .content("결제가 완료되었습니다.")
                .isRead(false)
                .build());

        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notification.getId())
                        .cookie(accessTokenCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("알림 전체 읽음 처리 성공")
    void t8() throws Exception {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(NotificationType.PAYMENT_DONE)
                .content("결제1")
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(user)
                .type(NotificationType.CAMPING_APPROVED)
                .content("결제2")
                .isRead(false)
                .build());

        mockMvc.perform(patch("/api/notifications/read-all")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("모든 알림을 읽었습니다."));
    }

    @Test
    @DisplayName("알림 전체 읽음 처리 실패 - 미로그인")
    void t9() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isUnauthorized());
    }
}
