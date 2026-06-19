"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { CampingDetail } from "@/types/camping";

interface Props {
  camping: CampingDetail;
}

export default function ReservationCard({ camping }: Props) {
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const router = useRouter();

  const handleReservation = () => {
    if (!checkIn || !checkOut) {
      alert("체크인/체크아웃 날짜를 선택해주세요.");
      return;
    }
    if (checkIn >= checkOut) {
      alert("체크아웃은 체크인보다 이후 날짜여야 합니다.");
      return;
    }
    router.push(
      `/campings/${camping.id}/reservation?checkIn=${checkIn}&checkOut=${checkOut}`
    );
  };

  return (
    <aside>
      <div
        className={`
          sticky
          top-24
          bg-white
          rounded-3xl
          shadow-xl
          p-8
        `}
      >
        {/* 가격 */}
        <div className="text-3xl font-bold text-gray-900">
          ₩
          {Math.min(...camping.sites.map((site) => site.price)).toLocaleString()}
          <span className="text-lg font-normal text-gray-500">~</span>
        </div>

        {/* 날짜 선택 */}
        <div className="mt-6 space-y-3">
          
          <div>
            <label className="text-sm text-gray-500">체크인</label>
            <input
              type="date"
              value={checkIn}
              onChange={(e) => setCheckIn(e.target.value)}
              className={`
                mt-1
                w-full
                border
                border-gray-200
                rounded-xl
                px-3
                py-2
                text-gray-700
                focus:outline-none
                focus:ring-2
                focus:ring-[#4B6945]
              `}
            />
          </div>

          <div>
            <label className="text-sm text-gray-500">체크아웃</label>
            <input
              type="date"
              value={checkOut}
              onChange={(e) => setCheckOut(e.target.value)}
              className={`
                mt-1
                w-full
                border
                border-gray-200
                rounded-xl
                px-3
                py-2
                text-gray-700
                focus:outline-none
                focus:ring-2
                focus:ring-[#4B6945]
              `}
            />
          </div>

        </div>

        {/* 버튼 */}
        <button
        onClick={handleReservation}
          className={`
            mt-8
            w-full
            bg-[#CC7C35]
            hover:bg-[#a9632a]
            transition
            text-white
            py-4
            rounded-2xl
            font-bold
          `}
        >
          예약하기
        </button>
      </div>
    </aside>
  );
}