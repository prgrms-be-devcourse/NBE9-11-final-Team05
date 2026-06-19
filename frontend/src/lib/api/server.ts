import "server-only";
import { cookies } from "next/headers";
import { apiFetch } from "./core";

// 로그인 시 응답으로 받은 토큰을 저장하는 쿠키 이름.
// 실제 백엔드/로그인 처리부에서 정한 이름으로 맞춰주세요.
export const AUTH_COOKIE_NAME = "accessToken";

/**
 * 서버 컴포넌트, 서버 액션, route handler에서 사용.
 * 쿠키에 저장된 토큰을 읽어 Authorization 헤더에 실어 보낸다.
 */
export async function serverApiFetch<T>(
  path: string,
  options: Parameters<typeof apiFetch>[1] = {}
): Promise<T> {
  const cookieStore = await cookies();
  const token = cookieStore.get(AUTH_COOKIE_NAME)?.value;

  const res = await apiFetch<{ message: string; data: T }>(path, { ...options, token });
  return res.data;
}