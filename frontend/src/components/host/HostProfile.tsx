"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getHostProfile } from "@/lib/api/host";
import { HostProfileResponse } from "@/types/host";
import { useAuthStore } from "@/stores/authStore";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

const ERROR_MESSAGES: Record<string, string> = {
  INVALID_PASSWORD: "비밀번호가 일치하지 않습니다.",
  USER_NOT_FOUND: "존재하지 않는 회원입니다.",
  INTERNAL_SERVER_ERROR: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
  MISSING_REQUIRED_FIELD: "필수 입력 항목입니다.",
};

export default function HostProfile() {
  const router = useRouter();
  const { clearAuth } = useAuthStore();

  const [profile, setProfile] = useState<HostProfileResponse | null>(null);

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePassword, setDeletePassword] = useState("");
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    async function fetchProfile() {
      try {
        const data = await getHostProfile();
        setProfile(data);
      } catch (error) {
        console.error(error);
      }
    }

    fetchProfile();
  }, []);

  const handleDeleteAccount = async () => {
    if (!deletePassword.trim()) {
      alert("비밀번호를 입력해주세요.");
      return;
    }

    const confirmed = window.confirm(
      "정말 회원탈퇴 하시겠습니까?\n탈퇴 후 복구할 수 없습니다."
    );

    if (!confirmed) return;

    try {
      setDeleteLoading(true);

      const res = await fetch(`${API_URL}/api/users/me`, {
        method: "DELETE",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          password: deletePassword,
        }),
      });

      const result = await res.json();

      if (!res.ok) {
        const errorCode = result.code || result.message;
        const message = ERROR_MESSAGES[errorCode] || "오류가 발생했습니다.";
        throw new Error(message);
      }

      clearAuth();
      localStorage.removeItem("role");
      sessionStorage.clear();

      alert("회원탈퇴가 완료되었습니다.");
      window.location.replace("/");
    } catch (error) {
      alert(
        error instanceof Error
          ? error.message
          : "회원탈퇴 중 오류가 발생했습니다."
      );
    } finally {
      setDeleteLoading(false);
    }
  };

  if (!profile) {
    return (
      <div className="rounded-3xl border border-gray-100 bg-white p-8 shadow-sm">
        <p className="text-sm text-gray-500">
          프로필 정보를 불러오는 중...
        </p>
      </div>
    );
  }

  return (
    <>
      <section className="rounded-3xl border border-gray-100 bg-white p-8 shadow-sm">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-gray-900">
              호스트 정보
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              내 계정 정보를 확인하고 관리할 수 있습니다.
            </p>
          </div>

          <span className="rounded-full bg-[#FFF4EA] px-3 py-1 text-xs font-semibold text-[#D17A2F]">
            {profile.role}
          </span>
        </div>

        <div className="flex items-center gap-5">
          {profile.imageUrl ? (
            <img
              src={profile.imageUrl}
              alt={profile.nickname}
              width={88}
              height={88}
              className="h-22 w-22 rounded-full border-4 border-[#F4F5F1] object-cover"
            />
          ) : (
            <div className="flex h-22 w-22 items-center justify-center rounded-full bg-[#F4F5F1] text-sm font-medium text-gray-500">
              {profile.role}
            </div>
          )}

          <div className="flex-1">
            <p className="text-xl font-semibold text-gray-900">
              {profile.nickname}
            </p>

            <p className="mt-1 text-sm text-gray-500">
              {profile.phone}
            </p>

            <div className="mt-5 flex flex-wrap gap-2">
              <button
                onClick={() => router.push("/mypage/edit")}
                className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                프로필 정보 수정
              </button>

              <button
                onClick={() => router.push("/mypage/password")}
                className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                비밀번호 변경
              </button>

              <button
                onClick={() => setShowDeleteModal(true)}
                className="rounded-lg border border-red-100 px-4 py-2 text-sm font-medium text-red-500 hover:bg-red-50"
              >
                회원탈퇴
              </button>
            </div>
          </div>
        </div>
      </section>

      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6">
            <h3 className="mb-4 text-lg font-semibold text-gray-900">
              회원탈퇴
            </h3>

            <p className="mb-4 text-sm text-gray-500">
              회원탈퇴를 위해 비밀번호를 입력해주세요.
            </p>

            <input
              type="password"
              value={deletePassword}
              onChange={(e) => setDeletePassword(e.target.value)}
              placeholder="비밀번호"
              className="mb-4 w-full rounded-lg border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-red-300"
            />

            <div className="flex gap-2">
              <button
                onClick={() => {
                  setShowDeleteModal(false);
                  setDeletePassword("");
                }}
                className="flex-1 rounded-lg bg-gray-200 py-2 text-sm text-gray-700"
              >
                취소
              </button>

              <button
                onClick={handleDeleteAccount}
                disabled={deleteLoading}
                className="flex-1 rounded-lg bg-red-500 py-2 text-sm text-white disabled:opacity-50"
              >
                {deleteLoading ? "처리중..." : "탈퇴하기"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}