"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppToast } from "@/lib/ui/toast";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

interface UserProfile {
  id: number;
  nickname: string;
  imageUrl: string | null;
  phone: string;
  role: "USER" | "HOST" | "ADMIN";
  reservations: Reservation[];
  reviews: MyReviewResponse[];
}

interface Reservation {
  id: number;
  rsvNum: string;
  rsvName: string;
  rsvPhone: string;
  guestCount: number;
  request: string;
  campingName: string;
  siteName: string;
  address: string;
  imageUrl: string | null;
  checkIn: string;
  checkOut: string;
}

interface ReviewDetail {
  reviewId: number;
  rating: number;
  content: string;
}

interface MyReviewResponse {
  reservationId: number;
  campingId: number;
  campingName: string;
  campingImageUrl: string | null;
  checkIn: string;
  checkOut: string;
  reservationNumber: string;
  hasReview: boolean;
  review: ReviewDetail | null;
}

const WISHLIST_MOCK = ["강릉 어쩌구", "부산 어쩌구", "춘천 어쩌구"];

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

  // 회원탈퇴
  INVALID_PASSWORD: "비밀번호가 일치하지 않습니다.",
  USER_NOT_FOUND: "존재하지 않는 회원입니다.",

  // 공통
  INTERNAL_SERVER_ERROR: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
  MISSING_REQUIRED_FIELD: "필수 입력 항목입니다.",
};

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")} (${["일", "월", "화", "수", "목", "금", "토"][d.getDay()]})`;
}

export default function MyPage() {
  const router = useRouter();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePassword, setDeletePassword] = useState("");
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const res = await fetch(`${API_URL}/api/users/me`, {
          credentials: "include",
        });

        if (!res.ok) throw new Error("프로필 조회 실패");

        const data = await res.json();
        if (data.data.role === "HOST") {
          router.replace("/host/dashboard");
          return;
        }
        setProfile(data.data);
      } catch (e) {
        console.error("프로필 로딩 실패:", e);
        router.push("/auth/login");
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAF8F4]">
        <p className="text-gray-400 text-sm">불러오는 중...</p>
      </div>
    );
  }

  const handleDeleteAccount = async () => {
    if (!deletePassword.trim()) {
      AppToast.error("비밀번호를 입력해주세요.");
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

        const message =
          ERROR_MESSAGES[errorCode] ||
          "오류가 발생했습니다.";
  
        throw new Error(message);
      }
  
      localStorage.removeItem("role");
      sessionStorage.clear();
  
      AppToast.success("회원탈퇴가 완료되었습니다.");
  
      window.location.replace("/");
    } catch (error) {
      AppToast.error(
        error instanceof Error
          ? error.message
          : "회원탈퇴 중 오류가 발생했습니다."
      );
    } finally {
      setDeleteLoading(false);
    }
  };

  const reservations = profile?.reservations ?? [];
  const reviews = profile?.reviews ?? [];

  return (
    <div className="min-h-screen flex flex-col bg-[#FAF8F4]">

      <main className="flex-1 px-5 py-6 max-w-2xl mx-auto w-full flex flex-col gap-6">
        <h2 className="text-xl font-bold text-gray-900">마이 페이지</h2>

        {/* 상단: 프로필 + 위시리스트 */}
        <div className="w-full">
          {/* 프로필 정보 */}
          <div className="bg-[#EDE8DF] rounded-2xl p-4 flex flex-col items-center gap-3">
            <p className="text-sm font-semibold text-gray-700 self-start">프로필 정보</p>
            <div className="w-16 h-16 rounded-full bg-gray-300 overflow-hidden">
              {profile?.imageUrl ? (
                <img src={profile.imageUrl} alt="프로필" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full bg-gray-300" />
              )}
            </div>
            <div className="text-center">
              <p className="text-sm text-gray-700">
                닉네임: <span className="font-medium">{profile?.nickname ?? "-"}</span>
              </p>
              <p className="text-xs text-gray-500 mt-0.5">{profile?.phone ?? "-"}</p>
            </div>
            <button
              onClick={() => router.push("/mypage/edit")}
              className="text-xs text-gray-500 underline underline-offset-2 hover:text-gray-700 transition-colors"
            >
              프로필 정보 수정
            </button>
            <button
              onClick={() => router.push("/mypage/password")}
              className="text-xs text-gray-500 underline underline-offset-2 hover:text-gray-700 transition-colors"
            >
              비밀번호 변경
            </button>

            <button
              onClick={() => setShowDeleteModal(true)}
              className="text-xs text-red-500 underline underline-offset-2 hover:text-red-700 transition-colors"
            >
              회원탈퇴
            </button>
          </div>

          {/* 위시리스트
          <div className="flex flex-col gap-2">
            <p className="text-sm font-semibold text-gray-700">위시리스트</p>
            {WISHLIST_MOCK.map((item, i) => (
              <div
                key={i}
                className="bg-[#EDE8DF] rounded-xl px-4 py-3 text-sm text-gray-700 cursor-pointer hover:bg-[#E0D9CE] transition-colors"
              >
                {item}
              </div>
            ))}
          </div> */}
        </div>

        {/* 예약 내역 */}
        <div className="flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-gray-700">예약 내역</p>
            <button
              onClick={() => router.push("/mypage/reservations")}
              className="text-xs text-gray-500 hover:text-gray-700 transition-colors"
            >
              더보기
            </button>
          </div>

          {reservations.length === 0 ? (
            <div className="bg-[#EDE8DF] rounded-2xl p-6 text-center text-sm text-gray-400">
              예약 내역이 없습니다.
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {/* 첫 번째 예약 - 크게 */}
              {reservations[0] && (
                <div
                  className="bg-[#EDE8DF] rounded-2xl overflow-hidden cursor-pointer hover:brightness-95 transition"
                  onClick={() => router.push(`/mypage/reservations/${reservations[0].id}`)}
                >
                  <div className="relative h-36 bg-stone-300">
                    {reservations[0].imageUrl ? (
                      <img src={reservations[0].imageUrl} alt="캠핑장" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full bg-gradient-to-br from-stone-300 to-stone-400" />
                    )}
                  </div>
                  <div className="p-3">
                    <p className="font-semibold text-gray-800 text-sm">{reservations[0].campingName}</p>
                    <p className="text-xs text-gray-500 mt-0.5">{reservations[0].address}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      {formatDate(reservations[0].checkIn)} ~ {formatDate(reservations[0].checkOut)}
                    </p>
                  </div>
                </div>
              )}

              {/* 나머지 예약 - 작게 */}
              {reservations.slice(1, 3).map((r) => (
                <div
                  key={r.id}
                  className="bg-[#EDE8DF] rounded-2xl flex overflow-hidden cursor-pointer hover:brightness-95 transition"
                  onClick={() => router.push(`/mypage/reservations/${r.id}`)}
                >
                  <div className="w-20 h-20 bg-stone-300 shrink-0">
                    {r.imageUrl ? (
                      <img src={r.imageUrl} alt="캠핑장" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full bg-gradient-to-br from-stone-300 to-stone-400" />
                    )}
                  </div>
                  <div className="p-3 flex flex-col justify-center">
                    <p className="font-semibold text-gray-800 text-sm">{r.campingName}</p>
                    <p className="text-xs text-gray-500 mt-0.5">
                      {formatDate(r.checkIn)} ~ {formatDate(r.checkOut)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 최근 리뷰 */}
        <div className="flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-gray-700">최근 리뷰</p>
            <button
              onClick={() => router.push("/mypage/reviews")}
              className="text-xs text-gray-500 hover:text-gray-700 transition-colors"
            >
              더보기
            </button>
          </div>

          {reviews.length === 0 ? (
            <div className="bg-[#EDE8DF] rounded-2xl p-6 text-center text-sm text-gray-400">
              작성한 리뷰가 없습니다.
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3">
              {/* 왼쪽 - 첫 번째 리뷰 상세 */}
              {reviews[0] && reviews[0].hasReview && reviews[0].review && (
                <div className="bg-[#EDE8DF] rounded-2xl p-4 flex flex-col gap-1.5">
                  <p className="font-semibold text-gray-800 text-sm">{reviews[0].campingName}</p>
                  <p className="text-xs text-gray-500">- {reviews[0].review.rating}</p>
                  <p className="text-xs text-gray-600 leading-relaxed line-clamp-3 whitespace-pre-line">
                    {reviews[0].review.content}
                  </p>
                  <p className="text-xs text-gray-400 mt-auto">
                    {formatDate(reviews[0].checkIn)}
                  </p>
                </div>
              )}

              {/* 오른쪽 - 나머지 리뷰 캠핑명만 */}
              <div className="flex flex-col gap-3">
                {reviews.slice(1, 3).map((r) => (
                  <div key={r.reservationId} className="bg-[#EDE8DF] rounded-2xl px-4 py-3">
                    <p className="text-sm text-gray-700 font-medium">{r.campingName}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </main>

          {showDeleteModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-6 w-full max-w-sm">
            <h3 className="text-lg font-semibold mb-4">회원탈퇴</h3>
      
            <p className="text-sm text-gray-500 mb-4">
              회원탈퇴를 위해 비밀번호를 입력해주세요.
            </p>
      
            <input
              type="password"
              value={deletePassword}
              onChange={(e) => setDeletePassword(e.target.value)}
              placeholder="비밀번호"
              className="w-full border rounded-lg px-3 py-2 mb-4"
            />
      
            <div className="flex gap-2">
              <button
                onClick={() => {
                  setShowDeleteModal(false);
                  setDeletePassword("");
                }}
                className="flex-1 py-2 rounded-lg bg-gray-200"
              >
                취소
              </button>
      
              <button
                onClick={handleDeleteAccount}
                disabled={deleteLoading}
                className="flex-1 py-2 rounded-lg bg-red-500 text-white disabled:opacity-50"
              >
                {deleteLoading ? "처리중..." : "탈퇴하기"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

