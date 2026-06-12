package com.back.ovengers.global.response;

public record ApiResponse<T>(
        String message,
        T data
) {
    public ApiResponse(String message) {
        this(message, null);
    }
}