package com.back.ovengers.domain.chat.controller;

import com.back.ovengers.domain.chat.dto.ChatMessageRequest;
import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat-send")
    public void send(
            ChatMessageRequest request,
             Principal principal
    ) {
        Authentication authentication = (Authentication) principal;

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        User user = (User) authentication.getPrincipal();

        chatService.sendMessage(user, request);
    }
}
