package com.back.ovengers.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
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
    ALREADY_DELETED(
            HttpStatus.BAD_REQUEST,
            "탈퇴한 회원입니다."
    ),

    // 401
    INVALID_LOGIN_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),
    LOGIN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "로그인이 필요합니다."
    ),
    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않은 토큰입니다."
    ),

    // 403
    BANNED_USER(
            HttpStatus.FORBIDDEN,
            "이용이 정지된 계정입니다."
    ),
    HOST_REQUIRED(
            HttpStatus.FORBIDDEN,
            "호스트만 등록 가능합니다."
    ),
    NOT_CAMPING_OWNER(
            HttpStatus.FORBIDDEN,
            "해당 캠핑장에 대한 권한이 없습니다."
    ),

    // 404
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 회원입니다."
    ),
    CAMPING_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 캠핑장입니다."
    ),

    // 409
    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),
    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "이미 사용 중인 닉네임입니다."
    ),

    // 500
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String message;
}