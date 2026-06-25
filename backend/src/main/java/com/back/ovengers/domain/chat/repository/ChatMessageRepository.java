package com.back.ovengers.domain.chat.repository;

import com.back.ovengers.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        select m
            from ChatMessage m
                where m.roomId = :roomId
                  and (:cursor is null or m.id < :cursor)
                order by m.id desc
        """)
    List<ChatMessage> findChatMessages(Long roomId, Long cursor, PageRequest of);
}
