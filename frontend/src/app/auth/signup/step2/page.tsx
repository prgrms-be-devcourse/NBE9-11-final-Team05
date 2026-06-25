"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { signupApi } from "@/lib/api/signup";

export default function SignupStep2() {
  const router = useRouter();
  const [role, setRole] = useState<"user" | "host">("user");

  const [email, setEmail] = useState("");
  const [emailChecked, setEmailChecked] = useState(false);
  const [emailError, setEmailError] = useState("");

  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const passwordMatch = password !== "" && passwordConfirm !== "" && password === passwordConfirm;
  const passwordMismatch = passwordConfirm !== "" && password !== passwordConfirm;

  const [name, setName] = useState("");
  const [nickname, setNickname] = useState("");
  const [nicknameChecked, setNicknameChecked] = useState(false);
  const [nicknameError, setNicknameError] = useState("");

  const [phone, setPhone] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const savedRole = sessionStorage.getItem("signupRole") as "user" | "host" | null;
    if (savedRole) setRole(savedRole);
  }, []);

  const handleEmailCheck = async () => {
    if (!email.trim()) return;
    setEmailError("");
    try {
      await signupApi.checkEmail(email);
      setEmailChecked(true);
      setEmailError("사용 가능한 이메일입니다.");
    } catch (err) {
      setEmailChecked(false);
      setEmailError(err instanceof Error ? err.message : "이미 사용 중인 이메일입니다.");
    }
  };

  const handleNicknameCheck = async () => {
    if (!nickname.trim()) return;
    setNicknameError("");
    try {
      await signupApi.checkNickname(nickname);
      setNicknameChecked(true);
      setNicknameError("사용 가능한 닉네임입니다.");
    } catch (err) {
      setNicknameChecked(false);
      setNicknameError(err instanceof Error ? err.message : "이미 사용 중인 닉네임입니다.");
    }
  };

  const handleComplete = async () => {
    setError("");
    setLoading(true);
    try {
      await signupApi.signup({
        email,
        password,
        name,
        nickname,
        phone,
        role: role === "host" ? "HOST" : "USER",
      });
      sessionStorage.removeItem("signupRole");
      router.push("/auth/login");
    } catch (err) {
      console.error("회원가입 에러:", err);
      setError(err instanceof Error ? err.message : "회원가입에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const isFormValid =
    emailChecked && passwordMatch && name !== "" && nicknameChecked && phone !== "";

  return (
    <div className="min-h-screen flex flex-col bg-white">

      {/* 본문 */}
      <main className="flex-1 flex flex-col items-center justify-center px-6 py-10 gap-6 -mt-60">
        <h2 className="text-2xl font-bold text-gray-800">
          안녕하세요😀 먼저 아래 정보를 입력해주세요
        </h2>

        {/* 폼 카드 */}
        <div className="w-full max-w-md bg-[#5C7A5C] rounded-2xl px-8 py-7 flex flex-col gap-4">

          {/* 이메일 */}
          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-2">
              <label className="text-white text-sm w-16 shrink-0 text-right">이메일</label>
              <input
                type="email"
                placeholder="이메일을 입력해주세요"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setEmailChecked(false); setEmailError(""); }}
                className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
              <button
                onClick={handleEmailCheck}
                className="shrink-0 px-3 py-2 bg-[#4a6b4a] hover:bg-[#3d5c3d] text-white text-xs rounded-full transition-colors"
              >
                중복확인
              </button>
            </div>
            {emailError && (
              <div className="flex items-center gap-2">
                <div className="w-16 shrink-0" />
                <p className={`text-xs ${emailChecked ? "text-green-300" : "text-red-300"}`}>{emailError}</p>
              </div>
            )}
          </div>

          {/* 비밀번호 */}
          <div className="flex items-center gap-2">
            <label className="text-white text-sm w-16 shrink-0 text-right">비밀번호</label>
            <input
              type="password"
              placeholder="비밀번호를 입력해주세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
            />
            <div className="w-[68px]" />
          </div>

          {/* 비밀번호 확인 */}
          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-2">
              <div className="w-16 shrink-0" />
              <input
                type="password"
                placeholder="비밀번호 확인"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
                className={`flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2
                  ${passwordMismatch ? "focus:ring-red-400 ring-2 ring-red-300" : "focus:ring-orange-400"}`}
              />
              <div className="w-[68px]" />
            </div>
            {passwordMismatch && (
              <div className="flex items-center gap-2">
                <div className="w-16 shrink-0" />
                <p className="text-red-300 text-xs">비밀번호가 일치하지 않습니다.</p>
              </div>
            )}
          </div>

          {/* 이름 */}
          <div className="flex items-center gap-2">
            <label className="text-white text-sm w-16 shrink-0 text-right">이름</label>
            <input
              type="text"
              placeholder="이름을 입력해주세요"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
            />
            <div className="w-[68px]" />
          </div>

          {/* 닉네임 */}
          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-2">
              <label className="text-white text-sm w-16 shrink-0 text-right">닉네임</label>
              <input
                type="text"
                placeholder="닉네임을 입력해주세요"
                value={nickname}
                onChange={(e) => { setNickname(e.target.value); setNicknameChecked(false); setNicknameError(""); }}
                className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
              <button
                onClick={handleNicknameCheck}
                className="shrink-0 px-3 py-2 bg-[#4a6b4a] hover:bg-[#3d5c3d] text-white text-xs rounded-full transition-colors"
              >
                중복확인
              </button>
            </div>
            {nicknameError && (
              <div className="flex items-center gap-2">
                <div className="w-16 shrink-0" />
                <p className={`text-xs ${nicknameChecked ? "text-green-300" : "text-red-300"}`}>{nicknameError}</p>
              </div>
            )}
          </div>

          {/* 전화번호 */}
          <div className="flex items-center gap-2">
            <label className="text-white text-sm w-16 shrink-0 text-right">전화번호</label>
            <input
              type="tel"
              placeholder="010-1234-5678"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
            />
            <div className="w-[68px]" />
          </div>

        </div>

        {/* 에러 메시지 */}
        {error && <p className="text-red-500 text-sm">{error}</p>}

        {/* 버튼 */}
        <div className="w-full max-w-md">
          <button
            onClick={handleComplete}
            disabled={!isFormValid || loading}
            className={`w-full py-3 rounded-xl text-white font-bold text-base transition-colors
              ${isFormValid && !loading
                ? "bg-orange-400 hover:bg-orange-500 active:bg-orange-600"
                : "bg-orange-200 cursor-not-allowed"
              }`}
          >
            {loading ? "처리 중..." : "회원가입 완료!"}
          </button>
        </div>
      </main>

    </div>
  );
}