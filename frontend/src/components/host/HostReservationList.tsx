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

  const formatDateTime = (value: string) => {
    if (!value) return "-";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;

    return date.toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusStyle = (status: string) => {
    switch (status) {
      case "CONFIRMED":
        return "bg-[#EAF3E8] text-[#3F6B3F]";
      case "PENDING":
        return "bg-[#FFF4E8] text-[#D17A2F]";
      case "CANCELLED":
        return "bg-gray-100 text-gray-500";
      default:
        return "bg-[#F4F5F1] text-gray-600";
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case "CONFIRMED":
        return "예약 확정";
      case "PENDING":
        return "예약 대기";
      case "CANCELLED":
        return "예약 취소";
      default:
        return status;
    }
  };

  if (isLoading) {
    return (
      <div className="rounded-3xl border border-gray-100 bg-white p-8 shadow-sm">
        <p className="text-sm text-gray-500">예약 목록을 불러오는 중입니다...</p>
      </div>
    );
  }

  if (errorMessage) {
    return (
      <div className="rounded-3xl border border-red-100 bg-red-50 p-8">
        <p className="text-sm font-medium text-red-500">{errorMessage}</p>
      </div>
    );
  }

  return (
    <section className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">
          예약 현황
        </h1>
        <p className="mt-1 text-sm text-gray-500">
          내 캠핑장에 들어온 예약 목록을 확인할 수 있습니다.
        </p>
      </div>

      {reservations.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-gray-200 bg-white p-12 text-center shadow-sm">
          <p className="text-sm font-semibold text-gray-600">
            아직 들어온 예약이 없습니다.
          </p>
          <p className="mt-1 text-sm text-gray-400">
            예약이 생성되면 이곳에서 확인할 수 있습니다.
          </p>
        </div>
      ) : (
        <div className="space-y-5">
          {reservations.map((reservation) => (
            <article
              key={reservation.id}
              className="rounded-3xl border border-gray-100 bg-white p-7 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
            >
              <div className="mb-6 flex items-start justify-between gap-4 border-b border-gray-100 pb-5">
                <div>
                  <h2 className="text-xl font-bold text-gray-900">
                    {reservation.campingName}
                  </h2>
                  <p className="mt-2 text-sm font-medium text-gray-500">
                    {reservation.siteName} · 예약번호 {reservation.rsvNum}
                  </p>
                </div>

                <span
                  className={`shrink-0 rounded-full px-4 py-2 text-xs font-bold ${getStatusStyle(
                    reservation.status
                  )}`}
                >
                  {getStatusLabel(reservation.status)}
                </span>
              </div>

              <div className="grid gap-4 text-sm md:grid-cols-2">
                <InfoItem label="예약자" value={reservation.rsvName} />
                <InfoItem label="연락처" value={reservation.rsvPhone} />
                <InfoItem
                  label="이용 기간"
                  value={`${reservation.checkIn} ~ ${reservation.checkOut}`}
                />
                <InfoItem label="인원" value={`${reservation.guestCount}명`} />
                <InfoItem
                  label="예약 금액"
                  value={`${reservation.rsvPrice.toLocaleString()}원`}
                />
                <InfoItem
                  label="예약 생성일"
                  value={formatDateTime(reservation.createdAt)}
                />
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function InfoItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl bg-[#FAFAF7] px-4 py-3">
      <p className="text-xs font-semibold text-gray-400">{label}</p>
      <p className="mt-1 text-base font-semibold text-gray-800">{value || "-"}</p>
    </div>
  );
}