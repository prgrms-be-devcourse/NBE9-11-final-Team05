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
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  process.env.NEXT_PUBLIC_API_URL ??
  "http://localhost:8080";

interface RequestOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
}

/**
 * 서버/클라이언트 공통 fetch 코어.
 * - 서버 컴포넌트: serverApiFetch에서 Cookie 헤더를 headers에 담아서 호출
 * - 클라이언트 컴포넌트: credentials:"include"로 브라우저가 쿠키 자동 전송
 */
export async function apiFetch<T>(
  path: string,
  { body, headers, ...rest }: RequestOptions = {}
): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    method: rest.method ?? (body ? "POST" : "GET"),
    headers: {
      "Content-Type": "application/json",
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
