package com.back.ovengers.domain.notification.service;


import com.back.ovengers.domain.notification.dto.NotificationResponse;
import com.back.ovengers.domain.notification.dto.UnreadCountResponse;
import com.back.ovengers.domain.notification.entity.Notification;
import com.back.ovengers.domain.notification.entity.NotificationType;
import com.back.ovengers.domain.notification.repository.NotificationRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;

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

    @Test
    @DisplayName("알림을 생성할 수 있다")
    void test1() {
        // when
        notificationService.send(user, NotificationType.CAMPING_APPROVED, "캠핑장이 승인되었습니다.");

        // then
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.CAMPING_APPROVED);
        assertThat(notifications.get(0).getContent()).isEqualTo("캠핑장이 승인되었습니다.");
        assertThat(notifications.get(0).isRead()).isFalse();
    }

    @Test
    @DisplayName("알림 목록을 조회할 수 있다")
    void test2() {
        // given
        notificationService.send(user, NotificationType.CAMPING_APPROVED, "캠핑장이 승인되었습니다.");
        notificationService.send(user, NotificationType.PAYMENT_DONE, "결제가 완료되었습니다.");

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<NotificationResponse> result = notificationService.getNotifications(user.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("안 읽은 알림 개수를 조회할 수 있다")
    void test3() {
        // given
        notificationService.send(user, NotificationType.CAMPING_APPROVED, "캠핑장이 승인되었습니다.");
        notificationService.send(user, NotificationType.PAYMENT_DONE, "결제가 완료되었습니다.");

        // when
        UnreadCountResponse result = notificationService.getUnreadCount(user.getId());

        // then
        assertThat(result.unreadCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("알림 단건 읽음 처리할 수 있다")
    void test4() {
        // given
        notificationService.send(user, NotificationType.CAMPING_APPROVED, "캠핑장이 승인되었습니다.");

        Notification notification = notificationRepository.findAll().get(0);

        // when
        notificationService.read(user.getId(), notification.getId());

        // then
        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.isRead()).isTrue();
    }

    @Test
    @DisplayName("본인 알림이 아니면 읽음 처리할 수 없다")
    void test5() {
        // given
        notificationService.send(user, NotificationType.CAMPING_APPROVED, "캠핑장이 승인되었습니다.");
        Notification notification = notificationRepository.findAll().get(0);

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
        assertThatThrownBy(() -> notificationService.read(other.getId(), notification.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_ACCESS_DENIED);
    }

    @Test
    @DisplayName("알림 전체 읽음 처리할 수 있다")
    void test6() {
        // given
        notificationService.send(user, NotificationType.CAMPING_APPROVED, "캠핑장이 승인되었습니다.");
        notificationService.send(user, NotificationType.PAYMENT_DONE, "결제가 완료되었습니다.");

        // when
        notificationService.readAll(user.getId());

        // then
        assertThat(notificationRepository.countByUserIdAndIsReadFalse(user.getId())).isEqualTo(0L);
    }
}
