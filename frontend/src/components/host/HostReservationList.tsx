"use client";

import { useEffect, useState } from "react";
import { getHostReservations } from "@/lib/api/host";
import type { HostReservationListItem } from "@/types/host";

export default function HostReservationList() {
  const [reservations, setReservations] = useState<HostReservationListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    async function fetchReservations() {
      try {
        const data = await getHostReservations();
        setReservations(data);
      } catch {
        setErrorMessage("예약 목록을 불러오지 못했습니다.");
      } finally {
        setIsLoading(false);
      }
    }

    fetchReservations();
  }, []);

  if (isLoading) {
    return <p className="text-sm text-gray-500">예약 목록을 불러오는 중입니다...</p>;
  }

  if (errorMessage) {
    return <p className="text-sm text-red-500">{errorMessage}</p>;
  }

  if (reservations.length === 0) {
    return (
      <div className="rounded-lg border border-dashed border-gray-300 bg-white p-10 text-center">
        <p className="text-gray-600">예약 내역이 없습니다.</p>
      </div>
    );
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">예약 현황</h1>
        <p className="mt-1 text-sm text-gray-500">
          내 캠핑장에 들어온 예약 목록을 확인할 수 있습니다.
        </p>
      </div>

      <div className="space-y-4">
        {reservations.map((reservation) => (
          <article
            key={reservation.id}
            className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm"
          >
            <div className="mb-4 flex items-start justify-between gap-4">
              <div>
                <h2 className="text-lg font-semibold text-gray-900">
                  {reservation.campingName}
                </h2>
                <p className="mt-1 text-sm text-gray-500">
                  {reservation.siteName} · 예약번호 {reservation.rsvNum}
                </p>
              </div>

              <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
                {reservation.status}
              </span>
            </div>

            <div className="grid gap-3 text-sm text-gray-700 md:grid-cols-2">
              <p>예약자: {reservation.rsvName}</p>
              <p>연락처: {reservation.rsvPhone}</p>
              <p>
                이용 기간: {reservation.checkIn} ~ {reservation.checkOut}
              </p>
              <p>인원: {reservation.guestCount}명</p>
              <p>예약 금액: {reservation.rsvPrice.toLocaleString()}원</p>
              <p>예약 생성일: {reservation.createdAt}</p>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}