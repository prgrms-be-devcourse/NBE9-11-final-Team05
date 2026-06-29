"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/stores/authStore";
import { chatClient } from "@/lib/ws/chatClient";

export default function WebSocketProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const token = useAuthStore((s) => s.token);

  useEffect(() => {
    if (!token) {
      chatClient.disconnect();
      return;
    }

    chatClient.connect(token);

    return () => {
      // 페이지를 완전히 떠날 때만 실행
      chatClient.disconnect();
    };
  }, [token]);

  return <>{children}</>;
}