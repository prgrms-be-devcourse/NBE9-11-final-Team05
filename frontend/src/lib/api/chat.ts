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

/* 채팅방 목록 */
export async function getChatRooms(cursor?: number | null) {
  const url = new URL(`${API_URL}/api/chats/rooms`);

  if (cursor != null) {
    url.searchParams.append("cursor", String(cursor));
  }

  const res = await fetch(url.toString(), {
    credentials: "include",
    cache: "no-store",
  });

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message || "채팅방 목록 실패");
  }

  return result.data; 
}

/* DIRECT 채팅방 조회 (reservationId → roomId) */
export async function getDirectChatRoom(reservationId: number) {
  const url = new URL(
    `${API_URL}/api/chats/direct`
  );

  url.searchParams.append("reservationId", String(reservationId));

  const res = await fetch(url.toString(), {
    method: "GET",
    credentials: "include",
    cache: "no-store",
  });

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message || "DIRECT 채팅방 조회 실패");
  }

  return result.data; // { roomId }
}

/* 메시지 전송 (http) */
export async function sendChatMessage(roomId: number, content: string) {
  const res = await fetch(
    `${API_URL}/api/chats/messages`,
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

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message || "메시지 전송 실패");
  }

  return result.data;
}
