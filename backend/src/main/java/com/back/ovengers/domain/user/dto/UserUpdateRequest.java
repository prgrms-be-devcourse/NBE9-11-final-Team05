package com.back.ovengers.domain.user.dto;

import lombok.Getter;

@Getter
public class UserUpdateRequest {
    private String nickname;

    private String phone;

    private String imageUrl;
}
