"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { getReservationClient } from "@/lib/api/reservation.client";
import TossPaymentButton from "@/components/payment/TossPaymentButton";
import Card from "@/components/ui/Card";
import DataRow from "@/components/ui/DataRow";
import type { ReservationDetailResponse } from "@/types/reservation";

export default function ReservationDetailPage() {
  const searchParams = useSearchParams();
  const reservationId = searchParams.get("reservationId");

  const [reservation, setReservation] = useState<ReservationDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!reservationId) return;
    getReservationClient(Number(reservationId))
      .then(setReservation)
      .catch(() => setError("예약 정보를 불러오지 못했습니다."));
  }, [reservationId]);

  if (!reservationId) return <p className="p-10 text-center text-stone-500">예약 정보를 찾을 수 없습니다.</p>;
  if (error) return <p className="p-10 text-center text-red-500">{error}</p>;
  if (!reservation) return <p className="p-10 text-center text-stone-400">불러오는 중...</p>;

  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:3000";

  return (
    <div className="mx-auto grid max-w-4xl grid-cols-1 gap-6 p-6 md:grid-cols-2">
      <Card>
        <h2 className="mb-5 text-lg font-bold text-stone-900">결제 진행</h2>
        <p className="mb-3 font-semibold text-stone-900">{reservation.campingName}</p>
        <div className="divide-y divide-stone-100">
          <DataRow label="예약자 성함">{reservation.rsvName}</DataRow>
          <DataRow label="예약일">{reservation.checkIn} - {reservation.checkOut}</DataRow>
          <DataRow label="예약인원">{reservation.guestCount}명</DataRow>
          <DataRow label="예약구역">{reservation.siteName}</DataRow>
        </div>
        <div className="mt-5 flex items-baseline justify-between border-t border-stone-100 pt-4">
          <span className="text-sm text-stone-500">총 결제 금액</span>
          <span className="text-xl font-bold text-stone-900">
            {reservation.rsvPrice.toLocaleString()}원
          </span>
        </div>
      </Card>

      <Card className="flex flex-col">
        <h2 className="mb-5 text-lg font-bold text-stone-900">결제 수단</h2>
        <p className="mb-6 text-sm text-stone-500">
          아래 버튼을 누르면 토스페이먼츠 결제창이 열립니다.
        </p>
        <div className="mt-auto">
          <TossPaymentButton
            reservationId={reservation.id}
            displayAmount={reservation.rsvPrice}
            successUrl={`${baseUrl}/payment/complete`}
            failUrl={`${baseUrl}/payment/fail`}
          />
        </div>
      </Card>
    </div>
  );
}