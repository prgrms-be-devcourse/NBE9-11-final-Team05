"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getCampingReviewsClient } from "@/lib/api/review.client";
import type { ReviewResponse } from "@/types/review";

export default function CampingReviewsPage() {
  const params = useParams();
  const campingId = Number(params.campingId);

  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    // eslint-disable-next-line react-hooks/set-state-in-effect -- 페이지 변경 시 로딩 상태로 즉시 전환하기 위한 표준 데이터 페칭 패턴
    setLoading(true);
    setError(null);

    getCampingReviewsClient(campingId, page, 10)
      .then((res) => {
        if (cancelled) return;
        setReviews(res.content);
        setTotalElements(res.totalElements);
        setHasNext(res.hasNext);
      })
      .catch(() => {
        if (!cancelled) setError("후기를 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [campingId, page]);

  return (
    <div className="mx-auto max-w-2xl px-6 py-10">
      <h1 className="mb-6 text-2xl font-bold text-gray-900">
        후기 {totalElements > 0 && <span className="text-gray-400 text-lg">({totalElements})</span>}
      </h1>

      {loading && <p className="p-10 text-center text-gray-400">불러오는 중...</p>}
      {!loading && error && <p className="p-10 text-center text-red-500">{error}</p>}
      {!loading && !error && reviews.length === 0 && (
        <p className="py-16 text-center text-gray-400">아직 등록된 후기가 없습니다.</p>
      )}

      {!loading && !error && reviews.length > 0 && (
        <>
          <ul className="flex flex-col gap-4">
            {reviews.map((review) => (
              <li key={review.reviewId} className="rounded-3xl bg-white p-5 shadow-md">
                <div className="mb-2 flex items-center gap-2">
                  {review.writerImageUrl ? (
                    <img
                      src={review.writerImageUrl}
                      alt={review.writerNickname}
                      className="h-8 w-8 rounded-full object-cover"
                    />
                  ) : (
                    <div className="h-8 w-8 rounded-full bg-gray-200" />
                  )}
                  <span className="text-sm font-semibold text-gray-800">
                    {review.writerNickname}
                  </span>
                </div>
                <div className="mb-2 text-amber-500">
                  {"★".repeat(review.rating)}
                  <span className="text-gray-200">{"★".repeat(5 - review.rating)}</span>
                </div>
                <p className="whitespace-pre-wrap text-sm text-gray-600">{review.content}</p>
                <p className="mt-3 text-xs text-gray-400">{review.createdAt.slice(0, 10)}</p>
              </li>
            ))}
          </ul>

          {(page > 0 || hasNext) && (
            <div className="mt-6 flex items-center justify-center gap-4">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="flex h-9 w-9 items-center justify-center rounded-full border border-gray-200 text-gray-600 disabled:cursor-not-allowed disabled:opacity-40 hover:bg-gray-50"
              >
                ‹
              </button>
              <span className="text-sm text-gray-400">{page + 1}</span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={!hasNext}
                className="flex h-9 w-9 items-center justify-center rounded-full border border-gray-200 text-gray-600 disabled:cursor-not-allowed disabled:opacity-40 hover:bg-gray-50"
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
