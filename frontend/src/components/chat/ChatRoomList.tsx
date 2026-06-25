"use client";

import { useEffect, useState } from "react";
import { getChatRooms } from "@/lib/api/chat";
import { useChatStore } from "@/stores/chatStore";

export default function ChatRoomList() {
  const [rooms, setRooms] = useState<any[]>([]);
  const openChat = useChatStore((s) => s.openChat);

  useEffect(() => {
    const load = async () => {
      const res = await getChatRooms();
      setRooms(res.content);
    };

    load();
  }, []);

  return (
    <div className="p-2 space-y-2 overflow-y-auto h-full">
      {rooms.map((room) => (
        <div
          key={room.roomId}
          onClick={() => openChat(room.roomId)}
          className="
            p-3 border rounded cursor-pointer
            hover:bg-gray-100
            hover:scale-[1.01]
            transition
          "
        >
          <div className="font-bold">{room.roomName}</div>
          <div className="text-xs text-gray-500">
            {room.lastMessage}
          </div>
        </div>
      ))}
    </div>
  );
}