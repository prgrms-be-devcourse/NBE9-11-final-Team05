package com.back.ovengers.domain.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterServiceTest {

    private SseEmitterService sseEmitterService;

    @BeforeEach
    void setUp() {
        sseEmitterService = new SseEmitterService();
    }

    @Test
    @DisplayName("SSE 구독 - emitter 반환")
    void subscribe_returnsEmitter() {
        SseEmitter emitter = sseEmitterService.subscribe(1L);

        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("SSE 전송 - 구독 중인 유저에게 전송 성공")
    void send_toSubscribedUser() {
        sseEmitterService.subscribe(1L);

        // 예외 없이 전송 완료되면 성공
        sseEmitterService.send(1L, "테스트 알림");
    }

    @Test
    @DisplayName("SSE 전송 - 구독하지 않은 유저는 스킵")
    void send_toUnsubscribedUser_skips() {
        // 예외 없이 스킵되면 성공
        sseEmitterService.send(999L, "무시됨");
    }

    @Test
    @DisplayName("SSE 복수 구독 - 유저별 독립적으로 관리")
    void subscribe_multipleUsers() {
        SseEmitter emitter1 = sseEmitterService.subscribe(1L);
        SseEmitter emitter2 = sseEmitterService.subscribe(2L);

        assertThat(emitter1).isNotNull();
        assertThat(emitter2).isNotNull();
        assertThat(emitter1).isNotSameAs(emitter2);
    }
}
