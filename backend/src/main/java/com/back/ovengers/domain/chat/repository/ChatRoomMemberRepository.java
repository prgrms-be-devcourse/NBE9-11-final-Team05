package com.back.ovengers.domain.chat.repository;

import com.back.ovengers.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    @Query("""
            select m.userId
            from ChatRoomMember m
            where m.roomId = :roomId
        """)
    List<Long> findUserIdsByRoomId(Long roomId);
}
