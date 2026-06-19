export class ApiError extends Error {
  status: number;
  payload: unknown;

  constructor(status: number, message: string, payload?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

interface RequestOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
  token?: string; // 명시적으로 토큰을 넘기고 싶을 때 (서버 컴포넌트 등)
}

/**
 * 서버/클라이언트에서 공통으로 쓰는 fetch 코어.
 * - 서버 컴포넌트/route handler에서는 token을 직접 넘겨준다 (cookies()에서 읽어서).
 * - 클라이언트 컴포넌트에서는 credentials: "include"로 쿠키를 자동 전송한다.
 *   (쿠키 도메인/CORS 설정이 cross-origin이라면 백엔드에서 Access-Control-Allow-Credentials 필요)
 */


export async function apiFetch<T>(
  path: string,
  { body, token, headers, ...rest }: RequestOptions = {}
): Promise<T> {
// 임시 디버깅 — 확인 후 지워주세요
console.log("[apiFetch] url:", `${API_BASE_URL}${path}`);
console.log("[apiFetch] Authorization:", token ? `Bearer ${token.slice(0, 20)}...` : "없음");

  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    method: rest.method ?? (body ? "POST" : "GET"),
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Cookie: `accessToken=${token}` } : {}),
      ...headers,
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
    credentials: "include",
    cache: rest.cache ?? "no-store",
  });

  // 204 No Content 등 바디 없는 응답 처리
  const text = await res.text();
  const data = text ? safeJsonParse(text) : null;

  if (!res.ok) {
    const message =
      (data && typeof data === "object" && "message" in data
        ? String((data as { message?: unknown }).message)
        : undefined) ?? `요청에 실패했습니다. (status: ${res.status})`;
    throw new ApiError(res.status, message, data);
  }

  return data as T;
}

function safeJsonParse(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}