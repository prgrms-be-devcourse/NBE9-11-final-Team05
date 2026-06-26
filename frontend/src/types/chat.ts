export interface ChatJoinResponse {
    roomId: number;
}

export interface ChatMessageResponse {
    messageId: number;
    senderId: number;
    senderName: string;
    senderImageUrl: string;
    content: string;
    createdAt: string;
}

export interface ChatRoomResponse {
    roomId: number;
    roomName: string;
    lastMessage: string | null;
    type: "OPEN" | "DIRECT";
    lastMessageAt: string | null;
  }

export interface CursorResponse<T> {
    content: T[];
    nextCursor: number | null;
    hasNext: boolean;
  }