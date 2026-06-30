"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { CampingDetail } from "@/types/camping";
import { UserRole } from "@/lib/utils/auth";
import { AppToast } from "@/lib/ui/toast";

interface Props {
  camping: CampingDetail;
  role: UserRole | null;
}

export default function ReservationCard({ camping, role }: Props) {
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  
  const router = useRouter();

  const today = new Date().toLocaleDateString("sv-SE");

  const getMinCheckOut = (checkIn: string) => {
    if (!checkIn) return "";
    const date = new Date(checkIn);
    date.setDate(date.getDate() + 1);
    return date.toISOString().split("T")[0];
  };

  const handleReservation = () => {
    // 🔥 로그인 체크
    if (role === null) {
      AppToast.error("로그인이 필요합니다.");
      router.push("/auth/login");
      return;
    }

    // 🔥 권한 체크
    if (role !== "USER") {
      AppToast.error("사용자만 예약이 가능합니다.");
      return;
    }

    // 🔥 날짜 체크
    if (!checkIn || !checkOut) {
      AppToast.error("체크인/체크아웃 날짜를 선택해주세요.");
      return;
    }

    if (checkOut <= checkIn) {
      AppToast.error("체크아웃은 체크인 다음날 이후여야 합니다.");
      return;
    }

    router.push(
      `/campings/${camping.id}/reservation?checkIn=${checkIn}&checkOut=${checkOut}`
    );
  };

  return (
    <aside>
      <div className="sticky top-24 bg-white rounded-3xl shadow-xl p-8">
        {/* 가격 */}
        <div className="text-3xl font-bold text-gray-900">
          {camping.sites?.length > 0 ? (
            <>
              ₩
              {Math.min(...camping.sites.map((site) => site.price)).toLocaleString()}
              <span className="text-lg font-normal text-gray-500">~</span>
            </>
          ) : (
            <span className="text-gray-400 text-xl font-medium">
              가격 정보 없음
            </span>
          )}
        </div>

        {/* 날짜 선택 */}
        <div className="mt-6 space-y-3">
          <div>
            <label className="text-sm text-gray-500">체크인</label>
            <input
              type="date"
              value={checkIn}
              min={today}
              onChange={(e) => {
                setCheckIn(e.target.value);
                if (checkOut && checkOut <= e.target.value) {
                  setCheckOut("");
                }
              }}
              className="mt-1 w-full border border-gray-200 rounded-xl px-3 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#4B6945]"
            />
          </div>

          <div>
            <label className="text-sm text-gray-500">체크아웃</label>
            <input
              type="date"
              value={checkOut}
              min={getMinCheckOut(checkIn)}
              onChange={(e) => setCheckOut(e.target.value)}
              className="mt-1 w-full border border-gray-200 rounded-xl px-3 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#4B6945]"
            />
          </div>
        </div>

        {/* 버튼 */}
        <button
          onClick={handleReservation}
          className="mt-8 w-full bg-[#CC7C35] hover:bg-[#a9632a] transition text-white py-4 rounded-2xl font-bold"
        >
          예약하기
        </button>
      </div>
    </aside>
  );
}