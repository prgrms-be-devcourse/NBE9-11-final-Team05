package com.back.ovengers.domain.user.repository;

import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByNickname(String nickname);

    // 관리자 대시보드
    long countByStatus(Status status);
}
