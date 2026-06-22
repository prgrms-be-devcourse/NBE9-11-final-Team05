package com.back.ovengers.domain.notification.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterService {

    // 일반 해시맵은 Tread-safe하지 않음
    // 서버 확장 시 Redis Pub/Sub 개념 도입
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 연결 타임아웃 용 변수, 메모리 누수 방지
    private static final long TIMEOUT = 30*60*1000L;

    // SSE 연결을 맺는 메서드
    public SseEmitter subscribe(Long userId){

        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitters.put(userId, emitter);

        // 연결 종료/타임아웃 에러 시 emitter 제거
        emitter.onCompletion(()->emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        // 더미 이벤트 발송
        // 없으면 일부 브라우저/프록시가 연결을 끊어버리는 경우 발생
        try{
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return emitter;
    }

    // 특정 유저에게 SSE로 알림 전송 담당 메서드
    public void send(Long userId, Object data){
        SseEmitter emitter = emitters.get(userId);

        // SSE 연결 안되어있으면 스킵
        // 알림은 DB에 저장되어 있으니 나중에 목록 조회로 확인 가능
        if(emitter == null) return;

        try{
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(data)
            );
        } catch (IOException e) {
            // 전송 실패시 연결 안됨으로 판단하고 제거
            emitters.remove(userId);
        }

    }
}
