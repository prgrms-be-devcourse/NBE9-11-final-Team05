package com.back.ovengers.fixture;

import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.User;

public class UserFixture {
    public static User.UserBuilder user() {
        return User.builder()
                .email("test@test.com")
                .name("테스트유저")
                .role(Role.USER);
    }

    public static User.UserBuilder host() {
        return User.builder()
                .email("host@test.com")
                .name("테스트호스트")
                .role(Role.HOST);
    }
}
