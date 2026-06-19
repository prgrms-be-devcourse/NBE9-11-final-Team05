const API_URL = process.env.NEXT_PUBLIC_API_URL;

const ERROR_MESSAGES: Record<string, string> = {
  // 로그인
  INVALID_LOGIN_CREDENTIALS: "이메일 또는 비밀번호가 올바르지 않습니다.",
  ALREADY_DELETED: "탈퇴한 회원입니다.",
  BANNED_USER: "이용이 정지된 계정입니다.",
  ACCESS_TOKEN_MISSING: "Access Token이 없습니다.",
  ACCESS_TOKEN_EXPIRED: "Access Token이 만료되었습니다.",
  REFRESH_TOKEN_MISSING: "Refresh Token이 없습니다.",
  REFRESH_TOKEN_EXPIRED: "Refresh Token이 만료되었습니다.",
  REFRESH_TOKEN_INVALID: "Refresh Token이 유효하지 않습니다.",
  INVALID_TOKEN: "유효하지 않은 토큰입니다.",
  LOGIN_REQUIRED: "로그인이 필요합니다.",
  // 공통
  INTERNAL_SERVER_ERROR: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
  MISSING_REQUIRED_FIELD: "필수 입력 항목입니다."
};

export const authApi = {
  login: async (email: string, password: string) => {
    const res = await fetch(`${API_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ email, password }),
    });

    const data = await res.json();

    if (!res.ok) {
      const rawMessage = data.message || "로그인 실패";
      throw new Error(ERROR_MESSAGES[rawMessage] ?? rawMessage);
    }

    return data;
  },
};