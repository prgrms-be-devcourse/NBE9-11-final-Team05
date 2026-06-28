package com.back.ovengers.domain.camping.entity;

public enum ImageSyncStatus {
    PENDING,
    DONE, // 이미지가 있든 없든 API 확인을 완료한 상태
    FAILED,
    NO_CONTENT
}
