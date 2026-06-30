"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getCampingReviewsClient } from "@/lib/api/review.client";
import type { ReviewResponse } from "@/types/review";

interface Props {
  campingId: number;
}

const PAGE_SIZE = 4;

export default function ReviewSection({ campingId }: Props) {
  const [page, setPage] = useState(0);
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    Promise.resolve().then(() => {
      if (cancelled) return;
      setIsLoading(true);
      setError(null);
    });

    getCampingReviewsClient(campingId, page, PAGE_SIZE)
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
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [campingId, page]);

  return (
    <section>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">
          후기 {totalElements > 0 && <span className="text-gray-400 text-lg">({totalElements})</span>}
        </h2>
        {totalElements > PAGE_SIZE && (
          <Link
            href={`/campings/${campingId}/reviews`}
            className="text-sm font-medium text-gray-500 hover:text-gray-700"
          >
            전체보기
          </Link>
        )}
      </div>

      {isLoading && (
        <div className="bg-white rounded-3xl shadow-md p-12 text-center text-gray-400">
          불러오는 중...
        </div>
      )}

      {!isLoading && error && (
        <div className="bg-white rounded-3xl shadow-md p-12 text-center text-red-500">
          {error}
        </div>
      )}

      {!isLoading && !error && reviews.length === 0 && (
        <div className="bg-white rounded-3xl shadow-md p-12 text-center text-gray-400">
          아직 등록된 후기가 없습니다.
        </div>
      )}

      {!isLoading && !error && reviews.length > 0 && (
        <div className="relative">
          <div className="grid gap-4 sm:grid-cols-2">
            {reviews.map((review) => (
              <div
                key={review.reviewId}
                className="rounded-3xl bg-white p-5 shadow-md"
              >
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
                <p className="whitespace-pre-wrap text-sm text-gray-600">
                  {review.content}
                </p>
                <p className="mt-3 text-xs text-gray-400">
                  {review.createdAt.slice(0, 10)}
                </p>
              </div>
            ))}
          </div>

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
        </div>
      )}
    </section>
  );
}
