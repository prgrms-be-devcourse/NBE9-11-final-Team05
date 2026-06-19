import { apiFetch } from "./core";
import type { ReservationResponse } from "@/types/reservation";

/** 예약 상세조회 (클라이언트 컴포넌트 — 결제완료 화면 등에서 사용, 쿠키 자동 전송) */
export function getReservationClient(reservationId: number) {
  return apiFetch<ReservationResponse>(`/api/reservations/${reservationId}`);
}