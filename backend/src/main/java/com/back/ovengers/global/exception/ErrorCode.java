package com.back.ovengers.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    MISSING_REQUIRED_FIELD(
            HttpStatus.BAD_REQUEST,
            "MISSING_REQUIRED_FIELD",
            "필수값이 누락되었습니다."
    ),

    INVALID_EMAIL_FORMAT(
            HttpStatus.BAD_REQUEST,
            "INVALID_EMAIL_FORMAT",
            "이메일 형식이 올바르지 않습니다."
    ),

    INVALID_PASSWORD_FORMAT(
            HttpStatus.BAD_REQUEST,
            "INVALID_PASSWORD_FORMAT",
            "비밀번호는 8자 이상이어야 합니다."
    ),

    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "DUPLICATE_EMAIL",
            "이미 사용 중인 이메일입니다."
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "DUPLICATE_NICKNAME",
            "이미 사용 중인 닉네임입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}