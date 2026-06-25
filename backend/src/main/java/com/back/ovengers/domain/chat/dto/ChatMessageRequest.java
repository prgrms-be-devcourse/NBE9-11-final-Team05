package com.back.ovengers.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
    @NotBlank(message = "메시지 내용을 입력하세요.")
    String content
) {
}
