package com.back.ovengers.domain.chat.controller;

import com.back.ovengers.domain.chat.dto.ChatMessageResponse;
import com.back.ovengers.domain.chat.dto.ChatRoomResponse;
import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.global.response.ApiResponse;
import com.back.ovengers.global.response.CursorResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ApiResponse<CursorResponse<ChatRoomResponse>> getChatRooms() {
        return null;
    }

    @GetMapping("/{roomId}/messages")
    public ApiResponse<CursorResponse<ChatMessageResponse>> getChatMessages(
            @PathVariable @Min(1) Long roomId
    ) {
        return null;
    }
}
