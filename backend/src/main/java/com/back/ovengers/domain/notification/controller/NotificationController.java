package com.back.ovengers.domain.notification.controller;

import com.back.ovengers.domain.notification.dto.NotificationResponse;
import com.back.ovengers.domain.notification.dto.UnreadCountResponse;
import com.back.ovengers.domain.notification.service.NotificationService;
import com.back.ovengers.domain.notification.service.SseEmitterService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification", description = "알람 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @Operation(summary = "SSE 구독", description = "SSE 연결을 맺고 실시간 알림을 수신")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal User user
    ){
        return sseEmitterService.subscribe(user.getId());
    }

    @Operation(summary = "알람 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal User user,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ){
        Page<NotificationResponse> notifications = notificationService.getNotifications(user.getId(), pageable);
        return ResponseEntity.ok(new ApiResponse<>(
                "알림 목록 조회 성공",
                PageResponse.from(notifications)
        ));
    }

    @Operation(summary = "안 읽은 알림 개수 조회")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "안 읽은 알림 개수 조회 성공",
                notificationService.getUnreadCount(user.getId())
        ));
    }

    @Operation(summary = "알림 단건 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> read(
            @AuthenticationPrincipal User user,
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable @Min(1) Long notificationId
    ) {
        notificationService.read(user.getId(), notificationId);
        return ResponseEntity.ok(new ApiResponse<>("알림을 읽었습니다."));
    }

    @Operation(summary = "알림 전체 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAll(
            @AuthenticationPrincipal User user
    ) {
        notificationService.readAll(user.getId());
        return ResponseEntity.ok(new ApiResponse<>("모든 알림을 읽었습니다."));
    }
}
