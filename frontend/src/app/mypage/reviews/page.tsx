"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getMyReviewsClient } from "@/lib/api/review.client";
import Card from "@/components/ui/Card";
import Badge from "@/components/ui/Badge";
import type { MyReviewResponse } from "@/types/review";

export default function MyReviewsPage() {
  const [content, setContent] = useState<MyReviewResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);

  useEffect(() => {
    let cancelled = false;

    // eslint-disable-next-line react-hooks/set-state-in-effect -- 페이지 변경 시 로딩 상태로 즉시 전환하기 위한 표준 데이터 페칭 패턴
    setLoading(true);
    setError(null);

    getMyReviewsClient(page, 10)
      .then((res) => {
        if (cancelled) return;
        setContent(res.content);
        setHasNext(res.hasNext);
      })
      .catch(() => {
        if (!cancelled) setError("리뷰 목록을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [page]);

  return (
    <div className="mx-auto max-w-2xl p-6">
      <h1 className="mb-6 text-xl font-bold text-stone-900">내 리뷰</h1>

      {loading && <p className="p-10 text-center text-stone-400">불러오는 중...</p>}
      {!loading && error && <p className="p-10 text-center text-red-500">{error}</p>}

      {!loading && !error && content.length === 0 && (
        <p className="py-16 text-center text-stone-400">이용 완료한 예약이 없습니다.</p>
      )}

      {!loading && !error && content.length > 0 && (
        <>
          <ul className="flex flex-col gap-4">
            {content.map((item) => (
              <li key={item.reservationId}>
                <Link href={`/mypage/reservations/${item.reservationId}`}>
                  <Card className="flex gap-4 p-4">
                    {item.campingImageUrl ? (
                      <img
                        src={item.campingImageUrl}
                        alt={item.campingName}
                        className="h-20 w-24 shrink-0 rounded-2xl object-cover"
                      />
                    ) : (
                      <div className="h-20 w-24 shrink-0 rounded-2xl bg-stone-100" />
                    )}
                    <div className="flex flex-1 flex-col gap-1">
                      <div className="flex items-start justify-between gap-2">
                        <h3 className="font-semibold text-stone-900">{item.campingName}</h3>
                        <Badge tone={item.hasReview ? "success" : "neutral"}>
                          {item.hasReview ? "작성 완료" : "작성 가능"}
                        </Badge>
                      </div>
                      <p className="text-sm text-stone-500">
                        {item.checkIn} - {item.checkOut}
                      </p>
                      {item.hasReview && item.review ? (
                        <p className="mt-1 line-clamp-2 text-sm text-stone-600">
                          <span className="text-amber-500">{"★".repeat(item.review.rating)}</span>{" "}
                          {item.review.content}
                        </p>
                      ) : (
                        <p className="mt-1 text-sm text-amber-700">리뷰를 작성해보세요</p>
                      )}
                    </div>
                  </Card>
                </Link>
              </li>
            ))}
          </ul>

          {(page > 0 || hasNext) && (
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
        </>
      )}
    </div>
  );
}
