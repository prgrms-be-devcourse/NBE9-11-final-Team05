"use client";

import { useEffect, useState } from "react";
import { getChatRooms } from "@/lib/api/chat";
import { useChatStore } from "@/stores/chatStore";
import { useAuthStore } from "@/stores/authStore";

export default function ChatRoomList() {
  const [rooms, setRooms] = useState<any[]>([]);

  const openChat = useChatStore((s) => s.openChat);
  const isLoggedIn = useAuthStore((s) => s.isLoggedIn);

  useEffect(() => {
    if (!isLoggedIn) {
      setRooms([]);
      return;
    }

    const load = async () => {
      try {
        const res = await getChatRooms();
        setRooms(res.content);
      } catch (e) {
        console.error(e);
      }
    };

    load();
  }, [isLoggedIn]);

  if (!isLoggedIn) {
    return (
      <div className="flex h-full items-center justify-center text-gray-500 text-sm">
        로그인 후 이용 가능합니다.
      </div>
    );
  }

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