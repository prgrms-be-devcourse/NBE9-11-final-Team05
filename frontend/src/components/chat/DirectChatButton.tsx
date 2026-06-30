"use client";

import { useChatStore } from "@/stores/chatStore";
import { getDirectChatRoom } from "@/lib/api/chat";
import Button from "@/components/ui/Button";
import { AppToast } from "@/lib/ui/toast";

export default function DirectChatButton({
  reservationId,
}: {
  reservationId: number;
}) {
  const openChat = useChatStore((s) => s.openChat);

  const handleClick = async () => {
    try {
      const res = await getDirectChatRoom(reservationId);
      openChat(res.roomId);
    } catch (e) {
      console.error(e);
      AppToast.error("채팅방을 불러오는 중 오류가 발생했습니다. 다시 시도해 주세요.");
    }
  };

  return (
    <Button
      onClick={handleClick}
      variant="primary"
      fullWidth
    >
      호스트에게 문의하기
    </Button>
  );
}