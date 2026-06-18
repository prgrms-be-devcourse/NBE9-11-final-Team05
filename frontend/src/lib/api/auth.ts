const API_URL = process.env.NEXT_PUBLIC_API_URL;

const ERROR_MESSAGES: Record<string, string> = {
  INVALID_LOGIN_CREDENTIALS: "이메일 또는 비밀번호가 올바르지 않습니다.",
  // 나중에 다른 에러코드 생기면 여기에 추가
};

export const authApi = {
  login: async (email: string, password: string) => {
    const res = await fetch(`${API_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include", // 쿠키 기반 JWT 사용 시 필수
      body: JSON.stringify({
        email,
        password,
      }),
    });

    const data = await res.json();

    if (!res.ok) {
      const rawMessage = data.message || "로그인 실패";
      throw new Error(ERROR_MESSAGES[rawMessage] ?? rawMessage);
    }

    return data;
  },
};