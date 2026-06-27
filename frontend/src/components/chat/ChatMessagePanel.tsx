"use client";

import { useEffect, useRef, useState } from "react";
import { getChatMessages } from "@/lib/api/chat";
import { chatClient } from "@/lib/ws/chatClient";
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

  /* =========================
     최초 메시지 로딩
  ========================= */
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

  /* =========================
     WebSocket 구독
  ========================= */
  useEffect(() => {
    const subscription = chatClient.subscribe(roomId, (message) => {
      setMessages((prev) => [...prev, message]);
      requestAnimationFrame(scrollToBottom);
    });

    return () => subscription?.unsubscribe();
  }, [roomId]);

  /* =========================
     input focus
  ========================= */
  useEffect(() => {
    requestAnimationFrame(() => {
      inputRef.current?.focus();
    });
  }, [roomId]);

  /* =========================
     스크롤 아래로
  ========================= */
  const scrollToBottom = () => {
    if (!scrollRef.current) return;
    scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
  };

  /* =========================
     과거 메시지 로딩
  ========================= */
  const loadMore = async () => {
    if (!hasNext || loadingMore || cursor === null) return;
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
  };

  const sendMessage = () => {
    if (!input.trim()) return;

    setInput("");

    requestAnimationFrame(scrollToBottom);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") sendMessage();
  };

  /* =========================
     시간 / 날짜 포맷
  ========================= */
  const formatTime = (dateStr: string) => {
    const d = new Date(dateStr);
    return `${d.getHours().toString().padStart(2, "0")}:${d
      .getMinutes()
      .toString()
      .padStart(2, "0")}`;
  };

  const formatDate = (dateStr: string) => {
    const d = new Date(dateStr);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(
      2,
      "0"
    )}-${String(d.getDate()).padStart(2, "0")}`;
  };

  /* =========================
     UI
  ========================= */
  return (
    <div className="flex flex-col h-full">

      {/* 메시지 영역 */}
      <div
        ref={scrollRef}
        onScroll={handleScroll}
        className="flex-1 overflow-y-auto p-3 space-y-1"
      >
        {messages.map((m, idx) => {
          const isMine = m.senderId === myUserId;

          const currentDate = formatDate(m.createdAt);
          const prevDate =
            idx > 0 ? formatDate(messages[idx - 1].createdAt) : null;

          const showDateDivider = currentDate !== prevDate;

          return (
            <div key={m.messageId}>

              {/* 날짜 구분선 */}
              {showDateDivider && (
                <div className="flex justify-center my-2">
                  <div className="text-xs bg-gray-100 px-3 py-1 rounded-full text-gray-500">
                    {currentDate}
                  </div>
                </div>
              )}

              {/* 메시지 */}
              <div
                className={`flex items-end gap-2 ${
                  isMine ? "justify-end" : "justify-start"
                }`}
              >
                {/* 상대만 프로필 */}
                {!isMine && (
                  <img
                    src={
                      m.senderImageUrl ||
                      "/images/default-profile.png"
                    }
                    alt="profile"
                    className="w-8 h-8 rounded-full object-cover"
                  />
                )}

                {/* 말풍선 */}
                <div
                  className={`max-w-[70%] px-3 py-1 rounded-lg text-sm`}
                >
                  {!isMine && (
                    <div className="text-xs font-bold mb-0.5">
                      {m.senderName}
                    </div>
                  )}

                  <div
                    className={
                      isMine
                        ? "bg-blue-500 text-white rounded-br-none px-3 py-2 rounded-lg"
                        : "bg-gray-200 text-black rounded-bl-none px-3 py-2 rounded-lg"
                    }
                  >
                    {m.content}
                  </div>

                  {/* 시간 (양쪽 다 표시) */}
                  <div
                    className={`text-[10px] mt-1 ${
                      isMine ? "text-right text-gray-400" : "text-gray-400"
                    }`}
                  >
                    {formatTime(m.createdAt)}
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* 입력창 */}
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
