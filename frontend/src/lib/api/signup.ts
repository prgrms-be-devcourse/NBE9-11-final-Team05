const API_URL = process.env.NEXT_PUBLIC_API_URL;

const ERROR_MESSAGES: Record<string, string> = {
  // 회원
  DUPLICATE_EMAIL: "이미 사용 중인 이메일입니다.",
  DUPLICATE_NICKNAME: "이미 사용 중인 닉네임입니다.",
  INVALID_EMAIL_FORMAT: "이메일 형식이 올바르지 않습니다.",
  INVALID_PASSWORD_FORMAT: "비밀번호는 8자 이상이어야 합니다.",
  EMPTY_NICKNAME: "닉네임은 공백일 수 없습니다.",
  EMPTY_PHONE: "전화번호는 공백일 수 없습니다.",
  MISSING_REQUIRED_FIELD: "필수값이 누락되었습니다.",
  ALREADY_DELETED: "탈퇴한 회원입니다.",
  // 공통
  INTERNAL_SERVER_ERROR: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
};

interface UserSignupRequest {
  email: string;
  password: string;
  name: string;
  nickname: string;
  phone: string;
}

interface HostSignupRequest extends UserSignupRequest {
  businessNum: string;
  campingName: string;
  address: string;
}

const handleResponse = async (res: Response) => {
    const contentType = res.headers.get("content-type");
    const isJson = contentType?.includes("application/json");
  
    if (!res.ok) {
      if (isJson) {
        const error = await res.json();
  
        // validation 에러는 message가 MISSING_REQUIRED_FIELD이고
        // data에 "fieldName: 실제 에러 메시지" 형태로 담겨옴
        if (error.message === "MISSING_REQUIRED_FIELD" && error.data) {
          // "password: 비밀번호는 8자 이상이어야 합니다." → "비밀번호는 8자 이상이어야 합니다."
          const detail = String(error.data).split(": ").slice(1).join(": ");
          throw new Error(detail || "필수값이 누락되었습니다.");
        }
  
        const rawMessage = error.message || "요청에 실패했습니다.";
        throw new Error(ERROR_MESSAGES[rawMessage] ?? rawMessage);
      }
      throw new Error(`서버 오류 (${res.status})`);
    }
  
    return isJson ? res.json() : null;
  };

export const signupApi = {
  // 이메일 중복 확인
  checkEmail: async (email: string) => {
    const res = await fetch(`${API_URL}/api/auth/check/email?email=${encodeURIComponent(email)}`, {
      method: "GET",
    });
    return handleResponse(res);
  },

  // 닉네임 중복 확인
  checkNickname: async (nickname: string) => {
    const res = await fetch(`${API_URL}/api/auth/check/nickname?nickname=${encodeURIComponent(nickname)}`, {
      method: "GET",
    });
    return handleResponse(res);
  },

  // 유저 회원가입
  signupUser: async (data: UserSignupRequest) => {
    const res = await fetch(`${API_URL}/api/auth/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },

  // 호스트 회원가입
  signupHost: async (data: HostSignupRequest) => {
    const res = await fetch(`${API_URL}/api/auth/signup/host`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },
};