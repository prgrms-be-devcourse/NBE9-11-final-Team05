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
    @DisplayName("SSE 전송 - emitter 완료 후 제거")
    void subscribe_onCompletion_removesEmitter() {
        SseEmitter emitter = sseEmitterService.subscribe(1L);
        emitter.complete();

        // 완료 후 재전송 시 스킵 (NPE 없음)
        sseEmitterService.send(1L, "완료 후 메시지");
    }
}
