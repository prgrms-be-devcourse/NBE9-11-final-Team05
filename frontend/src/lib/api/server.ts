import "server-only";
import { cookies } from "next/headers";
import { apiFetch } from "./core";

export const AUTH_COOKIE_NAME = "accessToken";

/**
 * 서버 컴포넌트, 서버 액션, route handler에서 사용.
 * Next.js cookies()로 읽은 쿠키 전체를 Cookie 헤더에 담아서 백엔드로 전달한다.
 * 클라이언트의 credentials:"include" 방식과 동일한 효과.
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

  const res = await apiFetch<{ message: string; data: T }>(path, {
    ...options,
    headers: {
      Cookie: cookieHeader,
      ...options.headers,
    },
  });

  return res.data;
}