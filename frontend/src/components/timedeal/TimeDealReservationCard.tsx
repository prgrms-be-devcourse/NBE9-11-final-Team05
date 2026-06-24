"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createTimeDealReservation, TimeDealResponse } from "@/lib/api/timedeal";

interface Props {
  deal: TimeDealResponse;
}

export default function TimeDealReservationCard({ deal }: Props) {
  const router = useRouter();
  const isSoldOut = deal.status === "SOLD_OUT" || deal.remaining === 0;
  const isUnavailable = deal.status !== "ACTIVE" || isSoldOut;

  const [form, setForm] = useState({
    rsvName: "",
    rsvPhone: "",
    guestCount: "1",
    request: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const set = (key: keyof typeof form) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
      setForm((prev) => ({ ...prev, [key]: e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const reservation = await createTimeDealReservation(deal.id, {
        rsvName: form.rsvName,
        rsvPhone: form.rsvPhone,
        guestCount: Number(form.guestCount),
        request: form.request || undefined,
      });
      router.push(
        `/campings/${deal.campingId}/reservation/detail?reservationId=${reservation.id}`
      );
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "예약에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const inputCls =
    "w-full px-4 py-2.5 rounded-2xl border border-gray-200 text-sm focus:outline-none " +
    "focus:ring-2 focus:ring-[#4B6945]/30 focus:border-[#4B6945] transition";

  const nights =
    Math.round(
      (new Date(deal.checkOut).getTime() - new Date(deal.checkIn).getTime()) /
        (1000 * 60 * 60 * 24)
    );
  const totalPrice = deal.dealPrice * nights;

  return (
    <aside className="sticky top-24">
      <div className="bg-white rounded-3xl shadow-lg p-6 border border-gray-100">

        {/* 가격 요약 */}
        <div className="mb-5">
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-[#4B6945]">
              {deal.dealPrice.toLocaleString("ko-KR")}원
            </span>
            <span className="text-sm text-gray-300 line-through">
              {deal.originalPrice.toLocaleString("ko-KR")}원
            </span>
            <span className="text-sm font-bold text-amber-500">{deal.discountRate}%↓</span>
          </div>
          <p className="text-xs text-gray-400 mt-0.5">1박 기준</p>
        </div>

        {/* 날짜 / 총 금액 */}
        <div className="bg-[#F6F8F4] rounded-2xl p-4 mb-5 space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-500">체크인</span>
            <span className="font-medium">{deal.checkIn}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">체크아웃</span>
            <span className="font-medium">{deal.checkOut}</span>
          </div>
          <div className="border-t border-gray-200 pt-2 flex justify-between">
            <span className="text-gray-500">
              {deal.dealPrice.toLocaleString()}원 × {nights}박
            </span>
            <span className="font-bold text-[#4B6945]">
              {totalPrice.toLocaleString("ko-KR")}원
            </span>
          </div>
        </div>

        {/* 재고 */}
        <div className="mb-5">
          <div className="flex justify-between text-xs text-gray-400 mb-1">
            <span>남은 자리</span>
            <span>
              <span className="font-semibold text-gray-700">{deal.remaining}</span>
              /{deal.quantity}개
            </span>
          </div>
          <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full ${
                isSoldOut ? "bg-gray-300" :
                deal.remaining / deal.quantity <= 0.3 ? "bg-amber-400" : "bg-[#4B6945]"
              }`}
              style={{
                width: `${Math.min(100, Math.round((deal.soldCount / deal.quantity) * 100))}%`,
              }}
            />
          </div>
        </div>

        {/* 예약 불가 상태 */}
        {isUnavailable ? (
          <div className="text-center py-4 rounded-2xl bg-gray-50 border border-gray-100">
            <p className="text-sm font-medium text-gray-400">
              {isSoldOut ? "매진된 타임딜입니다" :
               deal.status === "SCHEDULED" ? "아직 판매가 시작되지 않았습니다" :
               "종료된 타임딜입니다"}
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-3">
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">예약자 이름</label>
              <input
                type="text"
                value={form.rsvName}
                onChange={set("rsvName")}
                placeholder="홍길동"
                required
                className={inputCls}
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">연락처</label>
              <input
                type="tel"
                value={form.rsvPhone}
                onChange={set("rsvPhone")}
                placeholder="010-0000-0000"
                required
                className={inputCls}
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">인원</label>
              <select
                value={form.guestCount}
                onChange={set("guestCount")}
                required
                className={inputCls}
              >
                {Array.from({ length: 10 }, (_, i) => i + 1).map((n) => (
                  <option key={n} value={n}>{n}명</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">
                요청사항 <span className="text-gray-300">(선택)</span>
              </label>
              <textarea
                value={form.request}
                onChange={(e) => setForm((prev) => ({ ...prev, request: e.target.value }))}
                placeholder="늦게 도착할 예정입니다."
                rows={2}
                className={inputCls + " resize-none"}
              />
            </div>

            {error && (
              <p className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-xl border border-red-100">
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 bg-[#4B6945] hover:bg-[#3d5738] text-white font-semibold
                rounded-2xl transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "예약 중…" : "타임딜 예약하기"}
            </button>

            <p className="text-xs text-center text-gray-400">
              예약 후 결제 페이지로 이동합니다
            </p>
          </form>
        )}
      </div>
    </aside>
  );
}