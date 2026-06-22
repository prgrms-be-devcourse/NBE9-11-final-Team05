import "server-only";
import { cookies } from "next/headers";
import { decodeJwt } from "jose";
import { AUTH_COOKIE_NAME } from "@/lib/api/server";

export type UserRole = "USER" | "HOST" | "ADMIN";

interface JwtPayload {
  sub?: string;
  role?: UserRole;
  iat?: number;
  exp?: number;
}

/**
 * 서버 컴포넌트에서 현재 로그인한 유저의 role을 읽어온다.
 * 토큰이 없거나 파싱 실패 시 null 반환.
 */
export async function getUserRole(): Promise<UserRole | null> {
  try {
    const cookieStore = await cookies();
    const token = cookieStore.get(AUTH_COOKIE_NAME)?.value;
    if (!token) return null;

    const payload = decodeJwt(token) as JwtPayload;
    return payload.role ?? null;
  } catch {
    return null;
  }
}

/** 예약 가능한 유저인지 확인 (USER만 예약 가능) */
export async function canReserve(): Promise<boolean> {
  const role = await getUserRole();
  return role === null || role === "USER";
}