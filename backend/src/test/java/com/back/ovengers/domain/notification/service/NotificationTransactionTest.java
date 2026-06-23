package com.back.ovengers.domain.notification.service;

import com.back.ovengers.domain.notification.entity.NotificationType;
import com.back.ovengers.domain.notification.repository.NotificationRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class NotificationTransactionTest {

    @Autowired
    NotificationService notificationService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;

    @MockitoBean
    SseEmitterService sseEmitterService;

    User user;

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
    }

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[분리 전] SSE 푸시 실패 시 알림 저장도 롤백된다")
    void before_sseFailure_rollbackNotification() {
        // given
        // SSE 푸시 시 예외 발생하도록 Mock 설정
        doThrow(new RuntimeException("SSE 전송 실패"))
                .when(sseEmitterService).send(any(), any());

        // when & then
        assertThatThrownBy(() ->
                notificationService.send(user, NotificationType.CAMPING_APPROVED, "캠핑장이 승인되었습니다.")
        ).isInstanceOf(RuntimeException.class);

        // 알림이 롤백되어 저장 안 됐는지 확인
        assertThat(notificationRepository.findAll()).isEmpty();
        System.out.println("알림 저장 개수: " + notificationRepository.findAll().size());
    }
}