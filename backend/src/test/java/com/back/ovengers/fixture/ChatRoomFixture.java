package com.back.ovengers.fixture;

import com.back.ovengers.domain.chat.entity.ChatRoom;
import com.back.ovengers.domain.chat.enums.ChatRoomStatus;
import com.back.ovengers.domain.chat.enums.ChatRoomType;
import org.springframework.test.util.ReflectionTestUtils;

public class ChatRoomFixture {
    public static ChatRoom openRoom(Long id) {
        ChatRoom room = ChatRoom.builder()
                .name("오픈 채팅방")
                .type(ChatRoomType.OPEN)
                .status(ChatRoomStatus.ACTIVE)
                .build();

        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    public static ChatRoom directRoom(Long id) {
        ChatRoom room = ChatRoom.builder()
                .name("다이렉트 채팅방")
                .type(ChatRoomType.DIRECT)
                .status(ChatRoomStatus.ACTIVE)
                .build();

        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }
}
