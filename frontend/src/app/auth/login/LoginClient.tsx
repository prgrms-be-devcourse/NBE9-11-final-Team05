"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { authApi } from "@/lib/api/auth";
import { useAuthStore } from "@/stores/authStore";

export default function LoginClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const returnUrl = searchParams.get("returnUrl");

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function parseJwt(token: string) {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");

    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );

    return JSON.parse(jsonPayload);
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const data = await authApi.login(email, password);
      const role = data.data.role;

      const token = data.data.accessToken;
      const payload = parseJwt(token);
      const userId = Number(payload.sub);

      useAuthStore.getState().setAuth(role, userId, token);

      const safeReturnUrl =
        returnUrl && returnUrl.startsWith("/")
          ? returnUrl
          : null;

      if (role === "USER" && safeReturnUrl) {
        router.push(safeReturnUrl);
        return;
      }

      if (role === "HOST") router.push("/host/dashboard");
      else if (role === "ADMIN") router.push("/admin/dashboard");
      else router.push("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleKakaoLogin = () => {
    // TODO
  };

  const handleGoogleLogin = () => {
    // TODO
  };

  return (
    <div className="min-h-screen flex flex-col bg-white">
      <main className="flex-1 flex flex-col items-center justify-center px-4 pt-2 pb-60">
        <div className="w-full max-w-3xl flex rounded-2xl overflow-hidden shadow-lg">

          {/* 왼쪽 */}
          <div className="hidden md:block w-1/2 relative">
            <img
              src="/test.svg"
              alt="캠핑장 풍경"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-br from-stone-400 via-stone-500 to-stone-700 -z-10" />
          </div>

          {/* 오른쪽 */}
          <div className="w-full md:w-1/2 bg-[#5C7A5C] flex flex-col items-center justify-center px-8 py-10 gap-5">
            <h2 className="text-white text-2xl font-semibold tracking-widest">
              Login
            </h2>

            <form onSubmit={handleLogin} className="w-full flex flex-col gap-3">
              <input
                type="email"
                placeholder="이메일을 입력하세요"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-2.5 rounded-md bg-white/95 text-sm text-gray-700 outline-none focus:ring-2 focus:ring-orange-400"
              />

              <input
                type="password"
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-2.5 rounded-md bg-white/95 text-sm text-gray-700 outline-none focus:ring-2 focus:ring-orange-400"
              />

              {error && (
                <p className="text-red-300 text-xs text-center">{error}</p>
              )}

              <button
                type="submit"
                disabled={loading}
                className={`w-full py-2.5 text-white font-semibold rounded-md
                  ${loading
                    ? "bg-orange-300"
                    : "bg-orange-400 hover:bg-orange-500"
                  }`}
              >
                {loading ? "로그인 중..." : "로그인"}
              </button>
            </form>

            <p className="text-white/60 text-xs">
              아이디/비밀번호 찾기{" "}
              <a href="/auth/find" className="underline">
                찾아보기
              </a>
            </p>

            <div className="w-full flex flex-col gap-2">
              <button
                onClick={handleKakaoLogin}
                className="w-full py-2.5 bg-[#FEE500] rounded-md"
              >
                카카오 로그인
              </button>

              <button
                onClick={handleGoogleLogin}
                className="w-full py-2.5 bg-white rounded-md"
              >
                구글 로그인
              </button>

              <button
                onClick={() => router.push("/auth/signup/step1")}
                className="w-full py-2.5 bg-white/20 text-white rounded-md"
              >
                회원가입
              </button>
            </div>
          </div>

        </div>
      </main>
    </div>
  );
}