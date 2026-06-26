"use client";

import { useEffect } from "react";
import { useChatStore } from "@/stores/chatStore";
import ChatMessagePanel from "./ChatMessagePanel";
import ChatRoomList from "./ChatRoomList";

export default function ChatWindow() {
  const { isOpen, view, roomId, close, back } = useChatStore();

  // ESC 닫기
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") close();
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, close]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50" onClick={close}>
      {/* WINDOW */}
      <div
        className="absolute bottom-4 right-4 w-96 h-[500px] bg-white shadow-xl rounded-lg flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* HEADER */}
        <div className="p-2 border-b flex justify-between items-center">

          {/* LEFT (뒤로가기 or 제목) */}
          {view === "chat" ? (
            <button
              onClick={back}
              className="text-sm px-2 py-1 rounded hover:bg-gray-100 transition cursor-pointer"
            >
              ← 뒤로가기
            </button>
          ) : (
            <div className="font-semibold text-sm">
              채팅방 목록
            </div>
          )}

          {/* X 버튼 */}
          <button
            onClick={close}
            className="
              text-gray-500
              hover:text-black
              hover:bg-gray-100
              transition
              rounded
              w-7 h-7
              flex items-center justify-center
              cursor-pointer
            "
          >
            ✕
          </button>
        </div>

        {/* BODY */}
        <div className="flex-1 overflow-hidden">
          {view === "list" && <ChatRoomList />}
          {view === "chat" && roomId && (
            <ChatMessagePanel roomId={roomId} />
          )}
        </div>
      </div>
    </div>
  );
}