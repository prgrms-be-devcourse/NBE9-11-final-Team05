package com.back.ovengers.domain.chat.repository;

import com.back.ovengers.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<Long, ChatRoom> {
}
