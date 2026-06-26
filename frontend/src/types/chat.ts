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
    lastMessage: string;
    type: "OPEN" | "DIRECT";
    lastMessageAt: string;
  }

export interface CursorResponse<T> {
    content: T[];
    nextCursor: number | null;
    hasNext: boolean;
  }