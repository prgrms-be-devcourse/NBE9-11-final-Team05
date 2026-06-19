"use server";

import { redirect } from "next/navigation";
import { createReservation } from "@/lib/api/reservation";
import { ApiError } from "@/lib/api/core";
import type { CreateReservationRequest } from "@/types/reservation";

export interface ReservationFormState {
  error?: string;
}

export async function submitReservation(
  campingId: string,
  _prevState: ReservationFormState,
  formData: FormData
): Promise<ReservationFormState> {
  const payload: CreateReservationRequest = {
    siteId: Number(formData.get("siteId")),
    rsvName: String(formData.get("rsvName") ?? ""),
    rsvPhone: String(formData.get("rsvPhone") ?? ""),
    checkIn: String(formData.get("checkIn") ?? ""),
    checkOut: String(formData.get("checkOut") ?? ""),
    guestCount: Number(formData.get("guestCount")),
    request: String(formData.get("request") ?? "") || undefined,
  };

  if (!payload.siteId || !payload.rsvName || !payload.checkIn || !payload.checkOut) {
    return { error: "필수 입력값을 확인해주세요." };
  }

  let reservationId: number;
  try {
    const reservation = await createReservation(payload);
    reservationId = reservation.id;
  } catch (err) {
    if (err instanceof ApiError) {
      return { error: err.message };
    }
    return { error: "예약 생성 중 오류가 발생했습니다." };
  }

  // 결제 페이지(좌: 요약 / 우: 토스버튼)로 이동 — redirect는 try/catch 밖에서 호출
  redirect(
    `/campings/${campingId}/reservation/detail?reservationId=${reservationId}`
  );
}