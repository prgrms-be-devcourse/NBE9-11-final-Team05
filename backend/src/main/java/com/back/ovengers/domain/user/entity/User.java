package com.back.ovengers.domain.user.entity;

import com.back.ovengers.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    private String imageUrl;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime deletedAt;

    @Builder
    public User(
            String imageUrl,
            String email,
            String password,
            String name,
            String nickname,
            String phone,
            Role role,
            Status status
    ) {
        this.imageUrl = imageUrl;
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }


    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}