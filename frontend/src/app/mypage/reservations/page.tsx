"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getMyReservationsClient } from "@/lib/api/reservation.client";
import Card from "@/components/ui/Card";
import Badge from "@/components/ui/Badge";
import type { ReservationListItem } from "@/types/reservation";

const STATUS_BADGE: Record<
  string,
  { label: string; tone: "neutral" | "success" | "danger" }
> = {
  CONFIRMED: { label: "예약 완료", tone: "success" },
  CANCELLED: { label: "예약 취소됨", tone: "danger" },
  PENDING: { label: "결제 대기", tone: "neutral" },
  COMPLETED: { label: "이용 완료", tone: "neutral" },
};

export default function MyReservationsPage() {
  const [content, setContent] = useState<ReservationListItem[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);

  useEffect(() => {
    let cancelled = false;

    Promise.resolve().then(() => {
      if (cancelled) return;
      setLoading(true);
      setError(null);
    });

    getMyReservationsClient(page)
      .then((res) => {
        if (cancelled) return;
        // PENDING 제외
        setContent(res.content.filter((r) => r.status !== "PENDING"));
        setHasNext(res.hasNext);
      })
      .catch(() => {
        if (!cancelled) setError("예약 목록을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [page]);

  if (loading) return <p className="p-10 text-center text-stone-400">불러오는 중...</p>;
  if (error) return <p className="p-10 text-center text-red-500">{error}</p>;

  return (
    <div className="mx-auto max-w-2xl p-6">
      <h1 className="mb-6 text-xl font-bold text-stone-900">예약 내역 조회</h1>

      {content.length === 0 && (
        <p className="py-16 text-center text-stone-400">예약 내역이 없습니다.</p>
      )}

      <ul className="flex flex-col gap-4">
        {content.map((reservation) => {
          const badge = STATUS_BADGE[reservation.status] ?? {
            label: reservation.status,
            tone: "neutral" as const,
          };
          return (
            <li key={reservation.id}>
              <Card className="flex gap-4 p-4">
                {reservation.imageUrl ? (
                  <img
                    src={reservation.imageUrl}
                    alt={reservation.campingName}
                    className="h-20 w-24 shrink-0 rounded-2xl object-cover"
                  />
                ) : (
                  <div className="h-20 w-24 shrink-0 rounded-2xl bg-stone-100" />
                )}
                <div className="flex flex-1 flex-col gap-1">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-semibold text-stone-900">{reservation.campingName}</h3>
                    <Badge tone={badge.tone}>{badge.label}</Badge>
                  </div>
                  <p className="text-sm text-stone-500">
                    {reservation.checkIn} - {reservation.checkOut}
                  </p>
                  <Link
                    href={`/mypage/reservations/${reservation.id}`}
                    className="mt-1 self-start rounded-full bg-amber-50 px-4 py-1.5 text-xs font-semibold text-amber-700 hover:bg-amber-100"
                  >
                    예약 상세
                  </Link>
                </div>
              </Card>
            </li>
          );
        })}
      </ul>

      {content.length > 0 && (page > 0 || hasNext) && (
        <div className="mt-6 flex items-center justify-center gap-4">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="flex h-9 w-9 items-center justify-center rounded-full border border-stone-200 text-stone-600 disabled:cursor-not-allowed disabled:opacity-40 hover:bg-stone-50"
          >
            ‹
          </button>
          <span className="text-sm text-stone-400">{page + 1}</span>
          <button
            onClick={() => setPage((p) => p + 1)}
            disabled={!hasNext}
            className="flex h-9 w-9 items-center justify-center rounded-full border border-stone-200 text-stone-600 disabled:cursor-not-allowed disabled:opacity-40 hover:bg-stone-50"
          >
            ›
          </button>
        </div>
      )}
    </div>
  );
}