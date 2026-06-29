import "server-only";
import { cookies } from "next/headers";
import { apiFetch } from "./core";

export const AUTH_COOKIE_NAME = "accessToken";

/**
 * 서버 컴포넌트, 서버 액션, route handler에서 사용.
 * Next.js cookies()로 읽은 쿠키 전체를 Cookie 헤더에 담아서 백엔드로 전달한다.
 */
export async function serverApiFetch<T>(
  path: string,
  options: Parameters<typeof apiFetch>[1] = {}
): Promise<T> {
  const cookieStore = await cookies();
  const cookieHeader = cookieStore
    .getAll()
    .map((c) => `${c.name}=${c.value}`)
    .join("; ");

  // 쿠키가 없는 경우(비로그인) 빈 Cookie 헤더를 보내지 않음
  // 빈 헤더는 일부 WAS에서 400 Bad Request를 유발할 수 있음
  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string>),
  };
  if (cookieHeader) {
    headers["Cookie"] = cookieHeader;
  }

  const res = await apiFetch<{ message: string; data: T }>(path, {
    ...options,
    headers,
  });

  // 204 No Content 등 바디 없는 응답 시 res.data가 null일 수 있으므로 옵셔널 체이닝 사용
  return res?.data;
}