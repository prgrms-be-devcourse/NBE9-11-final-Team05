package com.back.ovengers.domain.user.repository;

import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchUsers(Role role, Status status, boolean includeDeleted, String keyword, Pageable pageable);
}
