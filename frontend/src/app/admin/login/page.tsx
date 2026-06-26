"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { authApi } from "@/lib/api/auth";
import { useAuthStore } from "@/stores/authStore";

export default function AdminLoginPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      const data = await authApi.login(email, password);

      if (data.data.role !== "ADMIN") {
        throw new Error("관리자 계정만 로그인할 수 있습니다.");
      }

      useAuthStore.getState().setAuth(data.data.role);

      router.push("/admin/dashboard");
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "로그인에 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F5F6F2] flex items-center justify-center px-4">
      <div className="w-full max-w-4xl overflow-hidden rounded-3xl shadow-xl flex bg-white">

        {/* 왼쪽 영역 */}
        <div className="hidden md:flex w-1/2 bg-[#5C7A5C] flex-col items-center justify-center px-10">
          <h1 className="text-4xl font-bold text-white">
            캠핑가잣
          </h1>

          <p className="mt-5 text-white/80 text-sm text-center leading-6">
            관리자 전용 페이지입니다.
            <br />
            승인, 회원 관리, 리뷰 관리,
            <br />
            정산 기능을 사용할 수 있습니다.
          </p>
        </div>

        {/* 오른쪽 로그인 폼 */}
        <div className="w-full md:w-1/2 px-10 py-14 flex flex-col justify-center">

          <div className="mb-8">
            <p className="text-sm text-gray-400">
              ADMIN
            </p>

            <h2 className="text-3xl font-bold text-gray-800 mt-2">
              관리자 로그인
            </h2>
          </div>

          <form
            onSubmit={handleLogin}
            className="flex flex-col gap-4"
          >
            <input
              autoFocus
              type="email"
              placeholder="관리자 이메일"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="px-4 py-3 rounded-xl border border-gray-200 outline-none focus:ring-2 focus:ring-[#5C7A5C]"
            />

            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="px-4 py-3 rounded-xl border border-gray-200 outline-none focus:ring-2 focus:ring-[#5C7A5C]"
            />

            <div className="h-5">
              {error && (
                <p className="text-sm text-red-500">
                  {error}
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="mt-2 py-3 rounded-xl bg-[#5C7A5C] text-white font-semibold hover:bg-[#4F694F] transition"
            >
              {loading ? "로그인 중..." : "로그인"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}