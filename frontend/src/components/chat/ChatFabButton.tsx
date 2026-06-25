"use client";

import { useChatStore } from "@/stores/chatStore";

export default function ChatFabButton() {
  const openList = useChatStore((s) => s.openList);

  return (
    <button
      onClick={openList}
      className="fixed bottom-5 right-5 w-14 h-14 rounded-full bg-green-500 text-white shadow-lg"
    >
      💬
    </button>
  );
}