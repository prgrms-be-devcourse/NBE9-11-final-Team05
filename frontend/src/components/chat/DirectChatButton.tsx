"use client";

import { useChatStore } from "@/stores/chatStore";
import { getDirectChatRoom } from "@/lib/api/chat";
import Button from "@/components/ui/Button";

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