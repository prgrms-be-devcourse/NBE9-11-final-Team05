import { create } from "zustand";

interface ChatState {
  isOpen: boolean;
  view: "list" | "chat";
  roomId: number | null;

  openList: () => void;
  openChat: (roomId: number) => void;
  back: () => void;
  close: () => void;
}

export const useChatStore = create<ChatState>((set) => ({
  isOpen: false,
  view: "list",
  roomId: null,

  openList: () =>
    set({
      isOpen: true,
      view: "list",
      roomId: null,
    }),

  openChat: (roomId) =>
    set({
      isOpen: true,
      view: "chat",
      roomId,
    }),

  back: () =>
    set({
      view: "list",
      roomId: null,
    }),

  close: () =>
    set({
      isOpen: false,
      view: "list",
      roomId: null,
    }),
}));