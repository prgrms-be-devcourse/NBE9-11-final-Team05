package com.back.ovengers.domain.chat.controller;

import com.back.ovengers.domain.chat.dto.ChatJoinResponse;
import com.back.ovengers.domain.chat.dto.ChatMessageRequest;
import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "채팅방 목록 조회",
            description = "현재 로그인한 사용자의 채팅방 목록을 커서 기반 페이지네이션으로 조회합니다."
    )
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<CursorResponse<ChatRoomResponse>>> getChatRooms(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorResponse<ChatRoomResponse> response = chatService.getChatRooms(user.getId(), cursor, size);

        return ResponseEntity.ok(
                new ApiResponse<>("채팅방 목록 조회 성공", response)
        );
    }

    @Operation(
            summary = "채팅 메시지 조회",
            description = "특정 채팅방의 메시지를 커서 기반 페이지네이션으로 조회합니다."
    )
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<CursorResponse<ChatMessageResponse>>> getChatMessages(
            @AuthenticationPrincipal User user,
            @PathVariable @Min(1) Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorResponse<ChatMessageResponse> response = chatService.getChatMessages(
                user.getId(),
                roomId,
                cursor,
                size
        );

        return ResponseEntity.ok(
                new ApiResponse<>("메시지 조회 성공", response)
        );
    }

    @Operation(
            summary = "오픈채팅 참여",
            description = "캠핑장의 오픈채팅방에 참여합니다. 이미 참여 중이라면 기존 채팅방 정보를 반환합니다."
    )
    @PostMapping("/campings/{campingId}/join")
    public ResponseEntity<ApiResponse<ChatJoinResponse>> joinOpenChat(
            @PathVariable @Min(1) Long campingId,
            @AuthenticationPrincipal User user
    ) {
        ChatJoinResponse response = chatService.joinOpenChat(campingId, user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>("채팅 참여 성공", response)
        );
    }

    @Operation(
            summary = "1:1 채팅방 조회",
            description = "예약 정보를 이용하여 구매자와 판매자의 1:1 채팅방을 조회합니다."
    )
    @GetMapping("/direct")
    public ResponseEntity<ApiResponse<ChatJoinResponse>> getDirectRoom(
            @AuthenticationPrincipal User user,
            @RequestParam @Min(1) Long reservationId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("DIRECT 채팅방 조회 성공",
                        chatService.getDirectRoom(user.getId(), reservationId)
                )
        );
    }

    @Operation(
            summary = "채팅 메시지 저장",
            description = "채팅방에 메시지를 저장합니다."
    )
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ChatMessageRequest request
    ) {
        ChatMessageResponse response =
                chatService.sendMessage(user, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("채팅 저장 성공", response));
    }

}
