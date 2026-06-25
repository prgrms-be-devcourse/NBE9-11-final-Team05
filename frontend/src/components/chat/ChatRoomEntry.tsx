"use client";

import { useRef, useState } from "react";
import { joinOpenChat, getChatMessages } from "@/lib/api/chat";
import { useAuthStore } from "@/stores/authStore";

export default function ChatRoomEntry({ campingId }: { campingId: number }) {
  const [open, setOpen] = useState(false);
  const [roomId, setRoomId] = useState<number | null>(null);

  const [messages, setMessages] = useState<any[]>([]);
  const [input, setInput] = useState("");

  const [cursor, setCursor] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  const scrollRef = useRef<HTMLDivElement | null>(null);

  const myUserId = Number(useAuthStore((state) => state.userId));

  if (myUserId === null) {
    return <div>로딩중...</div>;
  }

  const scrollToBottom = () => {
    requestAnimationFrame(() => {
      if (scrollRef.current) {
        scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
      }
    });
  };

  const enterChat = async () => {
    const join = await joinOpenChat(campingId);

    setRoomId(join.roomId);

    const res = await getChatMessages(join.roomId);

    setMessages([...res.content].reverse());
    setCursor(res.nextCursor);
    setHasNext(res.hasNext);

    setOpen(true);

    setTimeout(() => {
      scrollToBottom();
    }, 0);
  };

  const loadMore = async () => {
    if (!roomId || !hasNext || loadingMore || cursor === null) return;
    if (!scrollRef.current) return;

    const prevScrollHeight = scrollRef.current.scrollHeight;

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

      const newScrollHeight = scrollRef.current.scrollHeight;

      scrollRef.current.scrollTop =
        newScrollHeight - prevScrollHeight;
    });
  };

  const handleScroll = () => {
    if (!scrollRef.current) return;

    if (scrollRef.current.scrollTop < 50) {
      loadMore();
    }
  };

  const sendMessage = async () => {
    if (!input.trim() || !roomId) return;

    const content = input;
    setInput("");

    setMessages((prev) => [
      ...prev,
      {
        messageId: Date.now(),
        content,
        senderName: "나",
        senderId: myUserId,
      },
    ]);

    scrollToBottom();

    await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/api/chats/messages`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          roomId,
          content,
        }),
      }
    );
  };

  return (
    <>
      <button
        onClick={enterChat}
        className="w-full bg-green-500 text-white py-3 rounded-lg"
      >
        캠핑장 채팅 참여하기
      </button>

      {open && (
        <div className="fixed bottom-4 right-4 w-96 h-[500px] bg-white shadow-xl rounded-lg flex flex-col">

          {/* HEADER */}
          <div className="flex justify-between items-center p-3 border-b">
            <div className="font-bold">OPEN CHAT</div>
            <button onClick={() => setOpen(false)}>✕</button>
          </div>

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
                    className={`
                      max-w-[70%] px-3 py-2 rounded-lg text-sm break-words
                      ${
                        isMine
                          ? "bg-blue-500 text-white rounded-br-none"
                          : "bg-gray-200 text-black rounded-bl-none"
                      }
                    `}
                  >
                    {!isMine && (
                      <div className="text-xs font-bold mb-1">
                        {m.senderName}
                      </div>
                    )}

                    <div>{m.content}</div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* INPUT */}
          <div className="p-2 border-t flex gap-2">
            <input
              className="flex-1 border rounded px-2"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="메시지 입력"
            />
            <button
              onClick={sendMessage}
              className="bg-blue-500 text-white px-3 rounded"
            >
              전송
            </button>
          </div>
        </div>
      )}
    </>
  );
}