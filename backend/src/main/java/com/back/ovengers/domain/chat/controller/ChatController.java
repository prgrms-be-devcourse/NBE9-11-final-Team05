package com.back.ovengers.domain.chat.controller;

import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.CursorResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            @PathVariable @Min(1) Long roomId
    ) {
        return null;
    }
}
