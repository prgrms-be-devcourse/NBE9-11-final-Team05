"use client";

import { useState } from "react";
import {
  createReviewClient,
  deleteReviewClient,
  updateReviewClient,
} from "@/lib/api/review.client";
import type { ReviewDetail } from "@/types/review";
import Button from "@/components/ui/Button";

interface Props {
  reservationId: number;
  initialReview: ReviewDetail | null;
  onChanged: () => void;
}

const STAR_VALUES = [1, 2, 3, 4, 5];

export default function MyReservationReview({ reservationId, initialReview, onChanged }: Props) {
  const [review, setReview] = useState(initialReview);
  const [isEditing, setIsEditing] = useState(false);
  const [rating, setRating] = useState(review?.rating ?? 5);
  const [content, setContent] = useState(review?.content ?? "");
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const startEdit = () => {
    setRating(review?.rating ?? 5);
    setContent(review?.content ?? "");
    setError(null);
    setIsEditing(true);
  };

  const handleSubmit = async () => {
    if (!content.trim()) {
      setError("리뷰 내용을 입력해주세요.");
      return;
    }
    setIsPending(true);
    setError(null);
    try {
      if (review) {
        await updateReviewClient(review.reviewId, { rating, content });
        setReview({ reviewId: review.reviewId, rating, content });
      } else {
        await createReviewClient(reservationId, { rating, content });
        // 생성 응답에 reviewId가 없으므로 부모가 목록을 다시 조회해 실제 reviewId를 채워줌
        onChanged();
      }
      setIsEditing(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "리뷰 저장 중 오류가 발생했습니다.");
    } finally {
      setIsPending(false);
    }
  };

  const handleDelete = async () => {
    if (!review) return;
    setIsPending(true);
    setError(null);
    try {
      await deleteReviewClient(review.reviewId);
      setReview(null);
      setShowDeleteConfirm(false);
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "리뷰 삭제 중 오류가 발생했습니다.");
    } finally {
      setIsPending(false);
    }
  };

  if (isEditing) {
    return (
      <div className="rounded-2xl border border-stone-100 p-4">
        <div className="mb-3 flex gap-1">
          {STAR_VALUES.map((value) => (
            <button
              key={value}
              type="button"
              onClick={() => setRating(value)}
              className={`text-2xl leading-none ${value <= rating ? "text-amber-500" : "text-stone-200"}`}
              aria-label={`${value}점`}
            >
              ★
            </button>
          ))}
        </div>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
          placeholder="캠핑은 어떠셨나요?"
          className="w-full resize-none rounded-xl border border-stone-200 p-3 text-sm text-stone-700 focus:border-amber-400 focus:outline-none"
        />
        {error && <p className="mt-2 text-xs text-red-600">{error}</p>}
        <div className="mt-3 flex gap-2">
          <Button variant="ghost" fullWidth onClick={() => setIsEditing(false)} disabled={isPending}>
            취소
          </Button>
          <Button fullWidth onClick={handleSubmit} disabled={isPending}>
            {isPending ? "저장 중..." : "저장"}
          </Button>
        </div>
      </div>
    );
  }

  if (!review) {
    return (
      <div className="rounded-2xl border border-dashed border-stone-200 p-4 text-center">
        <p className="mb-3 text-sm text-stone-500">아직 작성한 리뷰가 없어요.</p>
        <Button onClick={startEdit}>리뷰 작성하기</Button>
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-stone-100 p-4">
      <div className="mb-2 text-amber-500">
        {"★".repeat(review.rating)}
        <span className="text-stone-200">{"★".repeat(5 - review.rating)}</span>
      </div>
      <p className="whitespace-pre-wrap text-sm text-stone-700">{review.content}</p>
      {error && <p className="mt-2 text-xs text-red-600">{error}</p>}
      <div className="mt-3 flex gap-2">
        <Button variant="ghost" fullWidth onClick={startEdit} disabled={isPending}>
          수정
        </Button>
        <Button
          variant="danger-outline"
          fullWidth
          onClick={() => setShowDeleteConfirm(true)}
          disabled={isPending}
        >
          삭제
        </Button>
      </div>

      {showDeleteConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="mx-4 w-full max-w-sm rounded-3xl bg-white p-6 shadow-xl">
            <h2 className="mb-2 text-center text-lg font-bold text-stone-900">
              리뷰를 삭제하시겠어요?
            </h2>
            <p className="mb-6 text-center text-sm text-stone-500">
              삭제 후에는 되돌릴 수 없습니다.
            </p>
            <div className="flex gap-3">
              <Button
                variant="ghost"
                fullWidth
                onClick={() => setShowDeleteConfirm(false)}
                disabled={isPending}
              >
                돌아가기
              </Button>
              <Button variant="danger-outline" fullWidth onClick={handleDelete} disabled={isPending}>
                {isPending ? "삭제 중..." : "삭제"}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
