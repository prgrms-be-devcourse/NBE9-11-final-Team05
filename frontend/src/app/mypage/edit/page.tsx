"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { signupApi } from "@/lib/api/signup";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

const ERROR_MESSAGES: Record<string, string> = {
  DUPLICATE_EMAIL: "이미 사용 중인 이메일입니다.",
  DUPLICATE_NICKNAME: "이미 사용 중인 닉네임입니다.",
  INVALID_EMAIL_FORMAT: "이메일 형식이 올바르지 않습니다.",
  INVALID_PASSWORD_FORMAT: "비밀번호는 8자 이상이어야 합니다.",
  EMPTY_NICKNAME: "닉네임은 공백일 수 없습니다.",
  EMPTY_PHONE: "전화번호는 공백일 수 없습니다.",
  MISSING_REQUIRED_FIELD: "필수값이 누락되었습니다.",
  ALREADY_DELETED: "탈퇴한 회원입니다.",
  INTERNAL_SERVER_ERROR: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
};

interface UserProfile {
  id: number;
  nickname: string;
  imageUrl: string | null;
  phone: string;
}

export default function EditProfilePage() {
  const router = useRouter();
  const [originalNickname, setOriginalNickname] = useState("");

  const [nickname, setNickname] = useState("");
  const [nicknameChecked, setNicknameChecked] = useState(false);
  const [nicknameError, setNicknameError] = useState("");

  const [phone, setPhone] = useState("");
  const [imageUrl, setImageUrl] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const res = await fetch(`${API_URL}/api/users/me`, {
          credentials: "include",
        });
        if (!res.ok) throw new Error("프로필 조회 실패");
        const data = await res.json();
        const p: UserProfile = data.data;
        setOriginalNickname(p.nickname ?? "");
        setNickname(p.nickname ?? "");
        setPhone(p.phone ?? "");
        setImageUrl(p.imageUrl ?? "");
      } catch (e) {
        console.error(e);
        router.push("/auth/login");
      } finally {
        setLoading(false);
      }
    };
    loadProfile();
  }, []);

  const nicknameChanged = nickname !== originalNickname;

  // 저장 가능 조건: 닉네임을 바꿨으면 중복확인 필수, 안 바꿨으면 바로 가능
  const canSave = nicknameChanged ? nicknameChecked : true;

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

  const handleSave = async () => {
    setError("");
    setSuccess(false);
    setSaving(true);
    try {
      const res = await fetch(`${API_URL}/api/users/me`, {
        method: "PATCH",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nickname,
          phone,
          imageUrl: imageUrl.trim() === "" ? null : imageUrl,
        }),
      });

      if (!res.ok) {
        const data = await res.json();
        const code = data?.code ?? "INTERNAL_SERVER_ERROR";
        setError(ERROR_MESSAGES[code] ?? "수정에 실패했습니다.");
        return;
      }

      setSuccess(true);
      setTimeout(() => router.push("/mypage"), 1000);
    } catch (e) {
      setError(ERROR_MESSAGES["INTERNAL_SERVER_ERROR"]);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAF8F4]">
        <p className="text-gray-400 text-sm">불러오는 중...</p>
      </div>
    );
  }

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
          <h2 className="text-xl font-bold text-gray-900">프로필 수정</h2>
        </div>

        <div className="bg-[#EDE8DF] rounded-2xl p-6 flex flex-col gap-6">
          {/* 프로필 이미지 미리보기 */}
          <div className="flex flex-col items-center gap-3">
            <div className="w-20 h-20 rounded-full bg-gray-300 overflow-hidden">
              {imageUrl ? (
                <img src={imageUrl} alt="프로필" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full bg-gray-300" />
              )}
            </div>
          </div>

          <div className="flex flex-col gap-4">
            {/* 이미지 URL */}
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700">프로필 이미지 URL</label>
              <input
                type="text"
                placeholder="이미지 URL을 입력해주세요"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                className="w-full px-3 py-2.5 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
            </div>

            {/* 닉네임 */}
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700">닉네임</label>
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  placeholder="닉네임을 입력해주세요"
                  value={nickname}
                  onChange={(e) => {
                    setNickname(e.target.value);
                    setNicknameChecked(false);
                    setNicknameError("");
                  }}
                  className="flex-1 px-3 py-2.5 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
                />
                {/* 닉네임이 바뀐 경우에만 중복확인 버튼 노출 */}
                {nicknameChanged && (
                  <button
                    onClick={handleNicknameCheck}
                    className="shrink-0 px-3 py-2.5 bg-[#4a6b4a] hover:bg-[#3d5c3d] text-white text-xs rounded-lg transition-colors"
                  >
                    중복확인
                  </button>
                )}
              </div>
              {nicknameError && (
                <p className={`text-xs ${nicknameChecked ? "text-green-600" : "text-red-500"}`}>
                  {nicknameError}
                </p>
              )}
              {/* 닉네임을 바꿨는데 아직 중복확인 안 한 경우 안내 */}
              {nicknameChanged && !nicknameChecked && !nicknameError && (
                <p className="text-xs text-orange-500">닉네임 중복확인이 필요합니다.</p>
              )}
            </div>

            {/* 전화번호 */}
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700">전화번호</label>
              <input
                type="tel"
                placeholder="010-1234-5678"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full px-3 py-2.5 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
            </div>
          </div>

          {error && <p className="text-red-500 text-sm text-center">{error}</p>}
          {success && <p className="text-green-600 text-sm text-center">수정이 완료되었습니다!</p>}

          <button
            onClick={handleSave}
            disabled={!canSave || saving}
            className={`w-full py-3 rounded-xl text-white font-bold text-base transition-colors
              ${canSave && !saving
                ? "bg-orange-400 hover:bg-orange-500 active:bg-orange-600"
                : "bg-orange-200 cursor-not-allowed"
              }`}
          >
            {saving ? "저장 중..." : "저장하기"}
          </button>
        </div>
      </main>
    </div>
  );
}