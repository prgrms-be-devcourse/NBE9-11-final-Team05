"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { getReservationClient } from "@/lib/api/reservation.client";
import { getMyReviewsClient } from "@/lib/api/review.client";
import CancelReservationButton from "@/components/reservation/CancelReservationButton";
import MyReservationReview from "@/components/review/MyReservationReview";
import Card from "@/components/ui/Card";
import DataRow from "@/components/ui/DataRow";
import Button from "@/components/ui/Button";
import type { ReservationDetailResponse } from "@/types/reservation";
import type { ReviewDetail } from "@/types/review";

export default function MyReservationDetailPage() {
  const params = useParams();
  const reservationId = String(params.reservationId);

  const [reservation, setReservation] = useState<ReservationDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [review, setReview] = useState<ReviewDetail | null>(null);
  const [reviewLoaded, setReviewLoaded] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);

  useEffect(() => {
    getReservationClient(Number(reservationId))
      .then(setReservation)
      .catch(() => setError("예약 정보를 불러오지 못했습니다."));
  }, [reservationId]);

  const loadReview = useCallback(() => {
    setReviewError(null);
    getMyReviewsClient(0, 100)
      .then((res) => {
        const matched = res.content.find(
          (item) => item.reservationId === Number(reservationId)
        );
        setReview(matched?.review ?? null);
      })
      .catch(() => setReviewError("리뷰 정보를 불러오지 못했습니다."))
      .finally(() => setReviewLoaded(true));
  }, [reservationId]);

  useEffect(() => {
    if (reservation?.status === "COMPLETED") {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- 예약 상태 확인 직후 리뷰를 즉시 조회하는 표준 데이터 페칭 패턴
      loadReview();
    }
  }, [reservation?.status, loadReview]);

  if (error) return <p className="p-10 text-center text-red-500">{error}</p>;
  if (!reservation) return <p className="p-10 text-center text-stone-400">불러오는 중...</p>;

  const isCancellable = reservation.status === "PENDING" || reservation.status === "CONFIRMED";
  const isCompleted = reservation.status === "COMPLETED";

  return (
    <div className="mx-auto max-w-md p-6">
      <Card>
        {reservation.imageUrl ? (
          <img
            src={reservation.imageUrl}
            alt={reservation.campingName}
            className="mb-4 h-40 w-full rounded-2xl object-cover"
          />
        ) : (
          <div className="mb-4 h-40 w-full rounded-2xl bg-stone-100" />
        )}

        <h2 className="mb-1 text-lg font-bold text-stone-900">{reservation.campingName}</h2>
        <p className="mb-4 text-xs text-stone-400">예약번호: {reservation.rsvNum}</p>

        <div className="divide-y divide-stone-100">
          <DataRow label="예약자">{reservation.rsvName}</DataRow>
          <DataRow label="예약일">{reservation.checkIn} - {reservation.checkOut}</DataRow>
          <DataRow label="구역">{reservation.siteName}</DataRow>
          <DataRow label="예약 인원">{reservation.guestCount}명</DataRow>
          <DataRow label="예약자 번호">{reservation.rsvPhone}</DataRow>
          <DataRow label="요청 사항">{reservation.request ?? "-"}</DataRow>
        </div>

        <div className="mt-4 flex items-baseline justify-between border-t border-stone-100 pt-4">
          <span className="text-sm text-stone-500">결제금액</span>
          <span className="text-lg font-bold text-stone-900">
            {reservation.rsvPrice.toLocaleString()}원
          </span>
        </div>
      </Card>

      {isCompleted && reviewLoaded && (
        <div className="mt-6">
          <h3 className="mb-3 text-sm font-semibold text-stone-900">내 리뷰</h3>
          {reviewError ? (
            <p className="text-sm text-red-500">{reviewError}</p>
          ) : (
            <MyReservationReview
              key={review?.reviewId ?? "new"}
              reservationId={Number(reservationId)}
              initialReview={review}
              onChanged={loadReview}
            />
          )}
        </div>
      )}

      <div className="mt-6 flex flex-col gap-3">
        <Link href={`/campings/${reservation.campingId}`} className="w-full">
          <Button variant="ghost" fullWidth>캠핑장 상세 페이지로 이동</Button>
        </Link>
        {isCancellable && (
          <CancelReservationButton reservationId={Number(reservationId)} />
        )}
      </div>
    </div>
  );
}