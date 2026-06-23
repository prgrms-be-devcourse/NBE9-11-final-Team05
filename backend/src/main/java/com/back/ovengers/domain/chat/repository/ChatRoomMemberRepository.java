package com.back.ovengers.domain.chat.repository;

import com.back.ovengers.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}
