"use client";

import { joinOpenChat } from "@/lib/api/chat";
import { useChatStore } from "@/stores/chatStore";
import { useAuthStore } from "@/stores/authStore";
import { AppToast } from "@/lib/ui/toast";

export default function ChatJoinButton({ campingId }: { campingId: number }) {
  const openChat = useChatStore((s) => s.openChat);
  const isLoggedIn = useAuthStore((s) => s.isLoggedIn);

  const enterChat = async () => {
    if (!isLoggedIn) {
      AppToast.error("로그인 후 이용 가능합니다.");
      return;
    }

    try {
      const res = await joinOpenChat(campingId);
      openChat(res.roomId);
      AppToast.success("채팅방에 입장했습니다.");
    } catch (e) {
      console.error(e);
      AppToast.error("채팅방 입장에 실패했습니다.");
    }
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