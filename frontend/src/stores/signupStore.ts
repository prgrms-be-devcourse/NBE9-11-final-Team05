import { create } from "zustand";

interface SignupState {
  email: string;
  password: string;
  name: string;
  nickname: string;
  phone: string;

  setSignupData: (data: Partial<SignupState>) => void;
  reset: () => void;
}

export const useSignupStore = create<SignupState>((set) => ({
  email: "",
  password: "",
  name: "",
  nickname: "",
  phone: "",

  setSignupData: (data) =>
    set((state) => ({
      ...state,
      ...data,
    })),

  reset: () =>
    set({
      email: "",
      password: "",
      name: "",
      nickname: "",
      phone: "",
    }),
}));