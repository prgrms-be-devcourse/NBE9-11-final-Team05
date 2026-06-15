package com.back.ovengers.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EMPTY_NICKNAME(
            HttpStatus.BAD_REQUEST,
            "닉네임은 공백일 수 없습니다."
    ),
    EMPTY_PHONE(
            HttpStatus.BAD_REQUEST,
            "전화번호는 공백일 수 없습니다."
    ),
    EMPTY_IMAGE_URL(
            HttpStatus.BAD_REQUEST,
            "프로필 이미지는 공백일 수 없습니다."
    ),

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
    ),

    // 호스트 도메인
    LOGIN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "로그인이 필요합니다."
    ),

    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않은 토큰입니다."
    ),

    HOST_REQUIRED(
            HttpStatus.FORBIDDEN,
            "호스트만 등록 가능합니다."
    ),

    NOT_CAMPING_OWNER(
            HttpStatus.FORBIDDEN,
            "해당 캠핑장에 대한 권한이 없습니다."
    ),

    CAMPING_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 캠핑장입니다."
    ),

    ACCESS_TOKEN_MISSING(
            HttpStatus.UNAUTHORIZED,
        "Access Token이 없습니다."
    ),

    ACCESS_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
        "Access Token이 만료되었습니다."
    ),

    // 예약
    RESERVATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "예약을 찾을 수 없습니다."
    ),
    RESERVATION_CONFLICT(
            HttpStatus.CONFLICT,
            "이미 예약된 날짜입니다."
    ),
    INVALID_RESERVATION_DATE(
            HttpStatus.BAD_REQUEST,
            "예약 날짜가 올바르지 않습니다."
    ),

    // 구역
    SITE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "구역을 찾을 수 없습니다."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 회원입니다."
    ),

    CAMPING_NOT_AVAILABLE(
            HttpStatus.BAD_REQUEST,
            "예약 불가능한 캠핑장입니다."
    ),

    GUEST_COUNT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "예약 인원이 최대 인원을 초과했습니다."
    ),

    SITE_NOT_AVAILABLE(
            HttpStatus.CONFLICT,
            "해당 구역의 재고가 없습니다."
    ),

    // 권한
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
        "접근 권한이 없습니다."
    ),
    // 리뷰
    REVIEW_NOT_FOUND(
            HttpStatus.NOT_FOUND,
        "리뷰를 찾을 수 없습니다."
    ),
    ALREADY_REVIEWED(
            HttpStatus.CONFLICT,
        "이미 리뷰를 작성한 예약입니다."
    ),
    RESERVATION_NOT_COMPLETED(
            HttpStatus.BAD_REQUEST,
        "이용 완료된 예약만 리뷰를 작성할 수 있습니다."
    ),
    REVIEW_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
        "리뷰 작성 권한이 없습니다."
    );

    private final HttpStatus status;
    private final String message;
}