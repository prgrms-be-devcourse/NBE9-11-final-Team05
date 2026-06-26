"use client";

import { useEffect, useState } from "react";
import { getChatRooms } from "@/lib/api/chat";
import { useChatStore } from "@/stores/chatStore";
import { useAuthStore } from "@/stores/authStore";
import { ChatRoomResponse } from "@/types/chat";
import { chatClient } from "@/lib/ws/chatClient";

type Tab = "ALL" | "DIRECT" | "OPEN";

export default function ChatRoomList() {
  const [rooms, setRooms] = useState<ChatRoomResponse[]>([]);
  const [tab, setTab] = useState<Tab>("ALL");

  const openChat = useChatStore((s) => s.openChat);
  const isLoggedIn = useAuthStore((s) => s.isLoggedIn);

  /* =========================
     1. 최초 로딩
  ========================= */
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

  /* =========================
     2. WebSocket (room list 실시간 업데이트)
  ========================= */
  useEffect(() => {
    if (!isLoggedIn) return;
  
    const userId = Number(useAuthStore.getState().userId);
  
    const subscription = chatClient.subscribeRoomList(
      userId,
      (updatedRoom: ChatRoomResponse) => {
        setRooms((prev) => {
          const map = new Map<number, ChatRoomResponse>();
  
          prev.forEach((r) => map.set(r.roomId, r));
          map.set(updatedRoom.roomId, updatedRoom);
  
          return Array.from(map.values()).sort(
            (a, b) =>
              new Date(b.lastMessageAt ?? 0).getTime() -
              new Date(a.lastMessageAt ?? 0).getTime()
          );
        });
      }
    );
  
    return () => subscription?.unsubscribe?.();
  }, [isLoggedIn]);

  if (!isLoggedIn) {
    return (
      <div className="flex h-full items-center justify-center text-gray-500 text-sm">
        로그인 후 이용 가능합니다.
      </div>
    );
  }

  /* =========================
     3. 필터링 (탭)
  ========================= */
  const filteredRooms = rooms.filter((r) => {
    if (tab === "ALL") return true;
    return r.type === tab;
  });

  const renderRoom = (room: ChatRoomResponse) => (
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
        {room.lastMessage ?? "메시지 없음"}
      </div>
    </div>
  );

  return (
    <div className="h-full flex flex-col">

      {/* TAB */}
      <div className="flex gap-2 p-2 border-b">
        <button
          onClick={() => setTab("ALL")}
          className={`text-xs px-2 py-1 rounded ${
            tab === "ALL" ? "bg-black text-white" : "bg-gray-100"
          }`}
        >
          전체
        </button>

        <button
          onClick={() => setTab("DIRECT")}
          className={`text-xs px-2 py-1 rounded ${
            tab === "DIRECT" ? "bg-black text-white" : "bg-gray-100"
          }`}
        >
          1:1
        </button>

        <button
          onClick={() => setTab("OPEN")}
          className={`text-xs px-2 py-1 rounded ${
            tab === "OPEN" ? "bg-black text-white" : "bg-gray-100"
          }`}
        >
          오픈
        </button>
      </div>

      {/* LIST */}
      <div className="p-2 space-y-2 overflow-y-auto flex-1">
        {filteredRooms.length === 0 ? (
          <div className="text-xs text-gray-400">
            채팅방이 없습니다
          </div>
        ) : (
          filteredRooms.map(renderRoom)
        )}
      </div>

    </div>
  );
}