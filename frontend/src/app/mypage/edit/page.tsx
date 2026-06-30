"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { signupApi } from "@/lib/api/signup";

const API_URL = process.env.NEXT_PUBLIC_API_URL;
const IMAGE_UPLOAD_URL = `${API_URL}/api/images/upload`; // 백엔드 업로드 엔드포인트 — 없으면 Base64 폴백

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
  role: "USER" | "HOST" | "ADMIN";
}

/** 이미지 파일을 서버에 업로드하고 URL을 반환. 엔드포인트가 없으면 Base64 폴백 */
async function uploadImage(file: File): Promise<string> {
  try {
    const formData = new FormData();
    formData.append("image", file);

    const res = await fetch(IMAGE_UPLOAD_URL, {
      method: "POST",
      credentials: "include",
      body: formData,
    });

    if (res.ok) {
      const data = await res.json();
      // 백엔드 응답 구조에 맞게 조정 (예: data.data.url 또는 data.url)
      return data?.data?.url ?? data?.url ?? "";
    }
  } catch {
    // 엔드포인트 없음 → Base64 폴백
  }

  // 폴백: Base64 Data URL
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error("파일 읽기 실패"));
    reader.readAsDataURL(file);
  });
}

export default function EditProfilePage() {
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [originalNickname, setOriginalNickname] = useState("");
  const [role, setRole] = useState<"USER" | "HOST" | "ADMIN">("USER");

  const [nickname, setNickname] = useState("");
  const [nicknameChecked, setNicknameChecked] = useState(false);
  const [nicknameError, setNicknameError] = useState("");

  const [phone, setPhone] = useState("");

  // 기존 imageUrl(서버 저장값) + 새로 선택한 파일/미리보기 분리
  const [savedImageUrl, setSavedImageUrl] = useState<string>("");
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string>("");

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
        setSavedImageUrl(p.imageUrl ?? "");
        setRole(p.role);
      } catch (e) {
        console.error(e);
        router.push("/auth/login");
      } finally {
        setLoading(false);
      }
    };
    loadProfile();
  }, []);

  // 파일 선택 핸들러
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 5MB 제한
    if (file.size > 5 * 1024 * 1024) {
      setError("이미지 크기는 5MB 이하여야 합니다.");
      return;
    }

    setImageFile(file);
    setError("");

    // 즉시 로컬 미리보기
    const reader = new FileReader();
    reader.onload = () => setImagePreview(reader.result as string);
    reader.readAsDataURL(file);
  };

  const handleRemoveImage = () => {
    setImageFile(null);
    setImagePreview("");
    setSavedImageUrl("");
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const displayImage = imagePreview || savedImageUrl; // 미리보기 우선, 없으면 기존 서버 이미지

  const nicknameChanged = nickname !== originalNickname;
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
      let finalImageUrl: string | null = savedImageUrl || null;
  
      if (imageFile) {
        try {
          finalImageUrl = await uploadImage(imageFile);
        } catch (uploadErr) {
          console.error("[이미지 업로드 실패]", uploadErr);
          setError("이미지 업로드에 실패했습니다. 다시 시도해주세요.");
          return;
        }
      }
  
      const body = { nickname, phone, imageUrl: finalImageUrl };
      console.log("[PATCH 요청 body]", body); // 확인용
  
      const res = await fetch(`${API_URL}/api/users/me`, {
        method: "PATCH",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
  
      if (!res.ok) {
        const data = await res.json();
        console.error("[PATCH 응답 에러]", data); // 확인용
        const code = data?.code ?? "INTERNAL_SERVER_ERROR";
        setError(ERROR_MESSAGES[code] ?? "수정에 실패했습니다.");
        return;
      }
  
      setSuccess(true);
      setTimeout(() => {
        router.push(role === "HOST" ? "/host/dashboard" : "/mypage");
      }, 1000);
    } catch (e) {
      console.error("[handleSave 예외]", e); // ← 여기서 실제 원인 확인
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
          {/* 프로필 이미지 */}
          <div className="flex flex-col items-center gap-3">
            <div className="w-20 h-20 rounded-full bg-gray-300 overflow-hidden">
              {displayImage ? (
                <img src={displayImage} alt="프로필" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full bg-gray-300" />
              )}
            </div>

            {/* 숨겨진 파일 input */}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleFileChange}
            />

            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="px-4 py-1.5 bg-[#4a6b4a] hover:bg-[#3d5c3d] text-white text-xs rounded-lg transition-colors"
              >
                사진 선택
              </button>
              {displayImage && (
                <button
                  type="button"
                  onClick={handleRemoveImage}
                  className="px-4 py-1.5 bg-gray-400 hover:bg-gray-500 text-white text-xs rounded-lg transition-colors"
                >
                  사진 삭제
                </button>
              )}
            </div>

            {imageFile && (
              <p className="text-xs text-gray-500">{imageFile.name}</p>
            )}
          </div>

          <div className="flex flex-col gap-4">
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