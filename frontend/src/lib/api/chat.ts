const API_URL = process.env.NEXT_PUBLIC_API_URL;

/* 채팅 참여 */
export async function joinOpenChat(campingId: number) {
  const res = await fetch(
    `${API_URL}/api/chats/campings/${campingId}/join`,
    {
      method: "POST",
      credentials: "include",
    }
  );

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message || "채팅 참여 실패");
  }

  return result.data; // { roomId }
}

/* 메시지 조회 (cursor 포함 가능하게 수정) */
export async function getChatMessages(roomId: number, cursor?: number | null) {
  const url = new URL(
    `${API_URL}/api/chats/rooms/${roomId}/messages`
  );

  if (cursor != null) {
    url.searchParams.append("cursor", String(cursor));
  }

  const res = await fetch(url.toString(), {
    credentials: "include",
    cache: "no-store",
  });

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message || "메시지 조회 실패");
  }

  return result.data; // CursorResponse
}