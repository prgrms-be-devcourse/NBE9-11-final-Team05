import { apiFetch } from "./core";
import type { PageResponse } from "@/types/host";
import type {
  MyReviewResponse,
  ReviewRequest,
  ReviewResponse,
} from "@/types/review";

/** 리뷰 작성 */
export async function createReviewClient(
  reservationId: number,
  payload: ReviewRequest
) {
  const res = await apiFetch<{ message: string; data: null }>(
    `/api/reviews?reservationId=${reservationId}`,
    { method: "POST", body: payload }
  );
  return res.message;
}

/** 리뷰 수정 */
export async function updateReviewClient(
  reviewId: number,
  payload: ReviewRequest
) {
  const res = await apiFetch<{ message: string; data: null }>(
    `/api/reviews/${reviewId}`,
    { method: "PUT", body: payload }
  );
  return res.message;
}

/** 리뷰 삭제 */
export async function deleteReviewClient(reviewId: number) {
  const res = await apiFetch<{ message: string; data: null }>(
    `/api/reviews/${reviewId}`,
    { method: "DELETE" }
  );
  return res.message;
}

/** 캠핑장 리뷰 목록 조회 (최신순) */
export async function getCampingReviewsClient(
  campingId: number,
  page: number = 0
) {
  const res = await apiFetch<{
    message: string;
    data: PageResponse<ReviewResponse>;
  }>(`/api/campings/${campingId}/reviews?page=${page}`);
  return res.data;
}

/** 내 리뷰 목록조회 (완료된 예약 + 작성 여부) */
export async function getMyReviewsClient(page: number = 0) {
  const res = await apiFetch<{
    message: string;
    data: PageResponse<MyReviewResponse>;
  }>(`/api/users/me/reviews?page=${page}`);
  return res.data;
}
