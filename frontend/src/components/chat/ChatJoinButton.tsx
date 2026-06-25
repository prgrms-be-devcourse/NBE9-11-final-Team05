"use client";

import { joinOpenChat } from "@/lib/api/chat";
import { useChatStore } from "@/stores/chatStore";

export default function ChatJoinButton({ campingId }: { campingId: number }) {
  const openChat = useChatStore((s) => s.openChat);

  const enterChat = async () => {
    const res = await joinOpenChat(campingId);

    openChat(res.roomId); // 바로 채팅방으로 이동
  };

  return (
    <button
      onClick={enterChat}
      className="w-full bg-green-500 text-white py-3 rounded-lg"
    >
      캠핑장 채팅 참여하기
    </button>
  );
}