package com.back.ovengers.domain.chat.controller;

import com.back.ovengers.domain.chat.dto.ChatMessageRequest;
import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.CursorResponse;
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
public class ChatController {

    private final ChatService chatService;

    @GetMapping
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

    @GetMapping("/{roomId}/messages")
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

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<Void>> joinOpenChat(
            @PathVariable @Min(1) Long roomId,
            @AuthenticationPrincipal User user
    ) {
        chatService.joinOpenChat(roomId, user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>("채팅 참여 성공")
        );
    }

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable @Min(1) Long roomId,
            @AuthenticationPrincipal User user,
            @RequestBody ChatMessageRequest request
    ) {
        ChatMessageResponse response = chatService.sendMessage(roomId, user, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("채팅 전송 성공", response)
        );
    }
}
