const API_URL = process.env.NEXT_PUBLIC_API_URL;

/* 로그인 유저 정보 */
export async function getMe() {
  const res = await fetch(`${API_URL}/api/users/me`, {
    credentials: "include",
  });

  if (!res.ok) {
    throw new Error("유저 정보 조회 실패");
  }

  const result = await res.json();
  return result;
}