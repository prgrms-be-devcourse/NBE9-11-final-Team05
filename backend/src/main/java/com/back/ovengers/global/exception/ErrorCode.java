package com.back.ovengers.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    MISSING_REQUIRED_FIELD(
            HttpStatus.BAD_REQUEST,
            "필수값이 누락되었습니다."
    ),

    INVALID_EMAIL_FORMAT(
            HttpStatus.BAD_REQUEST,
            "이메일 형식이 올바르지 않습니다."
    ),

    INVALID_PASSWORD_FORMAT(
            HttpStatus.BAD_REQUEST,
            "비밀번호는 8자 이상이어야 합니다."
    ),

    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "이미 사용 중인 닉네임입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    ),

    INVALID_LOGIN_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    ALREADY_DELETED(
            HttpStatus.BAD_REQUEST,
        "탈퇴한 회원입니다."
    ),

    BANNED_USER(
            HttpStatus.FORBIDDEN,
        "이용이 정지된 계정입니다."
    );

    private final HttpStatus status;
    private final String message;
}