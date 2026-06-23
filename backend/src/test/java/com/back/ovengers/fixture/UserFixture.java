package com.back.ovengers.fixture;

import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;

public class UserFixture {

    public static User.UserBuilder user() {
        return User.builder()
                .email("test@test.com")
                .password("password123!")
                .name("테스트유저")
                .nickname("테스트유저닉네임")
                .phone("010-1234-5678")
                .role(Role.USER)
                .status(Status.ACTIVE);
    }

    public static User.UserBuilder host() {
        return User.builder()
                .email("host@test.com")
                .password("password123!")
                .name("테스트호스트")
                .nickname("테스트호스트닉네임")
                .phone("010-9999-9999")
                .role(Role.HOST)
                .status(Status.ACTIVE);
    }
}