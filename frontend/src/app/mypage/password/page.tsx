"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

const ERROR_MESSAGES: Record<string, string> = {
  INVALID_PASSWORD: "현재 비밀번호가 올바르지 않습니다.",
  INVALID_PASSWORD_FORMAT: "비밀번호는 8자 이상이어야 합니다.",
  MISSING_REQUIRED_FIELD: "필수값이 누락되었습니다.",
  ALREADY_DELETED: "탈퇴한 회원입니다.",
  INTERNAL_SERVER_ERROR: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
};

export default function ChangePasswordPage() {
  const router = useRouter();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [newPasswordConfirm, setNewPasswordConfirm] = useState("");

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const passwordMatch = newPassword !== "" && newPasswordConfirm !== "" && newPassword === newPasswordConfirm;
  const passwordMismatch = newPasswordConfirm !== "" && newPassword !== newPasswordConfirm;
  const isFormValid = currentPassword !== "" && newPassword.length >= 8 && passwordMatch;

  const handleSave = async () => {
    setError("");
    setSuccess(false);
    setSaving(true);
    try {
      const res = await fetch(`${API_URL}/api/users/me/password`, {
        method: "PATCH",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ currentPassword, newPassword }),
      });

      if (!res.ok) {
        const data = await res.json();
        const code = data?.code ?? "INTERNAL_SERVER_ERROR";
        setError(ERROR_MESSAGES[code] ?? "비밀번호 변경에 실패했습니다.");
        return;
      }

      setSuccess(true);

      const profileRes = await fetch(`${API_URL}/api/users/me`, {
        credentials: "include",
      });

      if (profileRes.ok) {
        const profileData = await profileRes.json();
        const role = profileData.data?.role;

        setTimeout(() => {
          if (role === "HOST") {
            router.push("/host/dashboard");
            return;
          }

          router.push("/mypage");
        }, 1000);

        return;
      }

      setTimeout(() => router.push("/mypage"), 1000);
    } catch (e) {
      setError(ERROR_MESSAGES["INTERNAL_SERVER_ERROR"]);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#FAF8F4]">
      <main className="flex-1 px-5 py-6 max-w-2xl mx-auto w-full flex flex-col gap-6">
        <div className="flex items-center gap-3">
          <button
            onClick={() => router.back()}
            className="text-gray-500 hover:text-gray-700 transition-colors text-sm"
          >
            ← 돌아가기
          </button>
          <h2 className="text-xl font-bold text-gray-900">비밀번호 변경</h2>
        </div>

        <div className="bg-[#EDE8DF] rounded-2xl p-6 flex flex-col gap-4">
          {/* 현재 비밀번호 */}
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700">현재 비밀번호</label>
            <input
              type="password"
              placeholder="현재 비밀번호를 입력해주세요"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              className="w-full px-3 py-2.5 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
            />
          </div>

          {/* 새 비밀번호 */}
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700">새 비밀번호</label>
            <input
              type="password"
              placeholder="새 비밀번호를 입력해주세요 (8자 이상)"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full px-3 py-2.5 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
            />
            {newPassword !== "" && newPassword.length < 8 && (
              <p className="text-xs text-red-500">비밀번호는 8자 이상이어야 합니다.</p>
            )}
          </div>

          {/* 새 비밀번호 확인 */}
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700">새 비밀번호 확인</label>
            <input
              type="password"
              placeholder="새 비밀번호를 다시 입력해주세요"
              value={newPasswordConfirm}
              onChange={(e) => setNewPasswordConfirm(e.target.value)}
              className={`w-full px-3 py-2.5 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2
                ${passwordMismatch ? "ring-2 ring-red-300 focus:ring-red-400" : "focus:ring-orange-400"}`}
            />
            {passwordMismatch && (
              <p className="text-xs text-red-500">비밀번호가 일치하지 않습니다.</p>
            )}
            {passwordMatch && (
              <p className="text-xs text-green-600">비밀번호가 일치합니다.</p>
            )}
          </div>

          {error && <p className="text-red-500 text-sm text-center">{error}</p>}
          {success && <p className="text-green-600 text-sm text-center">비밀번호가 변경되었습니다!</p>}

          <button
            onClick={handleSave}
            disabled={!isFormValid || saving}
            className={`w-full py-3 rounded-xl text-white font-bold text-base transition-colors
              ${isFormValid && !saving
                ? "bg-orange-400 hover:bg-orange-500 active:bg-orange-600"
                : "bg-orange-200 cursor-not-allowed"
              }`}
          >
            {saving ? "변경 중..." : "비밀번호 변경"}
          </button>
        </div>
      </main>
    </div>
  );
}