import { create } from "zustand";
// persist: zustand 상태를 localStorage에 영구 저장하는 미들웨어
// 새로고침해도 로그인 상태 유지 가능
import { persist } from "zustand/middleware";

interface AuthState {
    isLoggedIn: boolean;   // 로그인 여부
    role: string | null;   // 유저 권한 (USER, HOST, ADMIN)
    userId: number | null;
    token: string | null;
    setAuth: (role: string, userId: number, token: string, ) => void;  // 로그인 시 호출
    clearAuth: () => void;            // 로그아웃 시 호출
}

export const useAuthStore = create<AuthState>()(
    // persist 미들웨어로 감싸면 상태가 localStorage에 자동 저장됨
    persist(
        (set) => ({
            isLoggedIn: false,
            role: null,
            userId: null,
            token: null,

            // 로그인 성공 시 role 저장하고 isLoggedIn = true
            setAuth: (role, userId, token) => {
                set({
                  isLoggedIn: true,
                  role,
                  userId,
                  token,
                });
              },

            // 로그아웃 시 상태 초기화
            clearAuth: () => {
                set({
                  isLoggedIn: false,
                  role: null,
                  userId: null,
                  token: null,
                });
              },
        }),
        {
            // localStorage에 저장될 키 이름
            name: "auth-storage",
        }
    )
);