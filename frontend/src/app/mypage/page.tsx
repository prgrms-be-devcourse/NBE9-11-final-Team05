"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

interface UserProfile {
  id: number;
  nickname: string;
  imageUrl: string | null;
  phone: string;
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

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")} (${["일", "월", "화", "수", "목", "금", "토"][d.getDay()]})`;
}

export default function MyPage() {
  const router = useRouter();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const res = await fetch(`${API_URL}/api/users/me`, {
          credentials: "include",
        });

        if (!res.ok) throw new Error("프로필 조회 실패");

        const data = await res.json();
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

  const reservations = profile?.reservations ?? [];
  const reviews = profile?.reviews ?? [];

  return (
    <div className="min-h-screen flex flex-col bg-[#FAF8F4]">
      {/* 헤더 */}
      <header className="flex items-center justify-between px-5 py-4 bg-white border-b border-gray-100">
        <h1 className="text-xl font-bold text-gray-900 tracking-tight">캠핑가잣</h1>
        <button aria-label="메뉴" className="flex flex-col gap-1.5 p-1">
          <span className="block w-6 h-0.5 bg-gray-800" />
          <span className="block w-6 h-0.5 bg-gray-800" />
          <span className="block w-6 h-0.5 bg-gray-800" />
        </button>
      </header>

      <main className="flex-1 px-5 py-6 max-w-2xl mx-auto w-full flex flex-col gap-6">
        <h2 className="text-xl font-bold text-gray-900">마이 페이지</h2>

        {/* 상단: 프로필 + 위시리스트 */}
        <div className="grid grid-cols-2 gap-4">
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
          </div>

          {/* 위시리스트 */}
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
          </div>
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
                  onClick={() => router.push(`/campings/${reservations[0].id}/reservation/detail`)}
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
                  onClick={() => router.push(`/campings/${r.id}/reservation/detail`)}
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

      {/* 푸터 */}
      <footer className="px-5 py-6 border-t border-gray-200 bg-white">
        <div className="max-w-2xl mx-auto flex flex-col md:flex-row md:justify-between gap-2">
          <div>
            <p className="font-semibold text-gray-800 text-sm">캠핑가잣</p>
            <p className="text-gray-400 text-xs mt-1">
              최고의 캠핑을 소개합니다 어쩌구.. 우리 캠핑<br />사이트 최고
            </p>
          </div>
          <div className="flex flex-col md:flex-row gap-1 md:gap-8 text-xs text-gray-400">
            <span>연락처: 어쩌구</span>
            <span>메일: 어쩌구@이쩌구.com</span>
          </div>
        </div>
      </footer>
    </div>
  );
}