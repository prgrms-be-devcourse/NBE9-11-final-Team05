package com.back.ovengers.domain.chat.repository;

import com.back.ovengers.domain.chat.dto.projection.ChatRoomSummary;
import com.back.ovengers.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        select
            r.id as id,
            r.name as name,
            m.content as content,
            m.createdAt as createdAt
        from ChatRoom r
        join ChatRoomMember rm
            on r.id = rm.roomId
        join ChatMessage m
            on m.id = (
                select max(cm.id)
                from ChatMessage cm
                where cm.roomId = r.id
            )
        where rm.userId = :userId
            and r.status = 'ACTIVE'
            and (:cursor is null or r.id < :cursor)
        order by r.id desc
    """)
    List<ChatRoomSummary> findChatRooms(Long userId, Long cursor, Pageable pageable);

}
