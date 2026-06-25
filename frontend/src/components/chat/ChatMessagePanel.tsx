"use client";

import { useEffect, useRef, useState } from "react";
import { getChatMessages, sendChatMessage } from "@/lib/api/chat";
import { useAuthStore } from "@/stores/authStore";

export default function ChatMessagePanel({
  roomId,
}: {
  roomId: number;
}) {
  const [messages, setMessages] = useState<any[]>([]);
  const [input, setInput] = useState("");

  const [cursor, setCursor] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  const scrollRef = useRef<HTMLDivElement | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const myUserId = Number(useAuthStore((s) => s.userId));

  // 1️⃣ 최초 로딩
  useEffect(() => {
    const load = async () => {
      const res = await getChatMessages(roomId);

      setMessages([...res.content].reverse());
      setCursor(res.nextCursor);
      setHasNext(res.hasNext);

      requestAnimationFrame(scrollToBottom);
    };

    load();
  }, [roomId]);

  // 👉 채팅방 들어오면 input 자동 focus
  useEffect(() => {
    requestAnimationFrame(() => {
      inputRef.current?.focus();
    });
  }, [roomId]);

  const scrollToBottom = () => {
    if (!scrollRef.current) return;

    scrollRef.current.scrollTop =
      scrollRef.current.scrollHeight;
  };

  // 3️⃣ 과거 메시지 로딩
  const loadMore = async () => {
    if (!roomId || !hasNext || loadingMore || cursor === null) return;
    if (!scrollRef.current) return;

    const prevHeight = scrollRef.current.scrollHeight;

    setLoadingMore(true);

    const res = await getChatMessages(roomId, cursor);

    setMessages((prev) => {
      const map = new Map();

      [...res.content, ...prev].forEach((m) => {
        map.set(m.messageId, m);
      });

      return Array.from(map.values()).sort(
        (a, b) => a.messageId - b.messageId
      );
    });

    setCursor(res.nextCursor);
    setHasNext(res.hasNext);
    setLoadingMore(false);

    requestAnimationFrame(() => {
      if (!scrollRef.current) return;

      const newHeight = scrollRef.current.scrollHeight;
      scrollRef.current.scrollTop = newHeight - prevHeight;
    });
  };

  const handleScroll = () => {
    if (!scrollRef.current) return;

    if (scrollRef.current.scrollTop < 50) {
      loadMore();
    }
  };

  // 4️⃣ 메시지 전송 (API 분리 적용 버전)
  const sendMessage = async () => {
    if (!input.trim()) return;

    const content = input;
    setInput("");

    const tempId = Date.now();

    // 1️⃣ optimistic update
    setMessages((prev) => [
      ...prev,
      {
        messageId: tempId,
        content,
        senderId: myUserId,
        senderName: "나",
      },
    ]);

    try {
      // 2️⃣ API 호출 (분리된 함수)
      await sendChatMessage(roomId, content);
    } catch (e) {
      console.error(e);

      // 실패 시 rollback
      setMessages((prev) =>
        prev.filter((m) => m.messageId !== tempId)
      );
    }

    requestAnimationFrame(scrollToBottom);
  };

  // 👉 Enter 전송
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      sendMessage();
    }
  };

  return (
    <div className="flex flex-col h-full">

      {/* MESSAGES */}
      <div
        ref={scrollRef}
        onScroll={handleScroll}
        className="flex-1 overflow-y-auto p-3 space-y-2"
      >
        {messages.map((m) => {
          const isMine = m.senderId === myUserId;

          return (
            <div
              key={m.messageId}
              className={`flex ${
                isMine ? "justify-end" : "justify-start"
              }`}
            >
              <div
                className={`max-w-[70%] px-3 py-2 rounded-lg text-sm
                  ${
                    isMine
                      ? "bg-blue-500 text-white rounded-br-none"
                      : "bg-gray-200 text-black rounded-bl-none"
                  }`}
              >
                {!isMine && (
                  <div className="text-xs font-bold mb-1">
                    {m.senderName}
                  </div>
                )}
                {m.content}
              </div>
            </div>
          );
        })}
      </div>

      {/* INPUT */}
      <div className="p-3 border-t flex gap-2">
        <input
          ref={inputRef}
          className="flex-1 border rounded px-3 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="메시지 입력"
        />

        <button
          onClick={sendMessage}
          className="bg-blue-500 text-white px-4 py-2 rounded"
        >
          전송
        </button>
      </div>
    </div>
  );
}