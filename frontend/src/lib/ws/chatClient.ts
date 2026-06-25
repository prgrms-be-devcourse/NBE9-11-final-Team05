import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

class ChatClient {
  private client: Client | null = null;

  connect(token: string) {
    if (this.client?.active) return;

    this.client = new Client({
      webSocketFactory: () =>
        new SockJS(`${process.env.NEXT_PUBLIC_API_URL}/ws`),

      reconnectDelay: 5000,

      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },

      debug: () => {},
    });

    this.client.activate();
  }

  subscribe(roomId: number, callback: (msg: any) => void) {
    if (!this.client) return;

    return this.client.subscribe(
      `/topic/chatroom/${roomId}`,
      (message: IMessage) => {
        callback(JSON.parse(message.body));
      }
    );
  }

  sendMessage(roomId: number, content: string) {
    if (!this.client) return;

    this.client.publish({
      destination: "/app/chat-send",
      body: JSON.stringify({
        roomId,
        content,
      }),
    });
  }

  disconnect() {
    this.client?.deactivate();
    this.client = null;
  }
}

export const chatClient = new ChatClient();