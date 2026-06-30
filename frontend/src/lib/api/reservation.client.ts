import { apiFetch } from "./core";
import type {
  CreateReservationRequest,
  ReservationCreateResponse,
  ReservationDetailResponse,
  ReservationListResponse,
} from "@/types/reservation";
import type { CampingDetail } from "@/types/camping";

/** 예약 생성 */
export async function createReservationClient(payload: CreateReservationRequest) {
  const res = await apiFetch<{ message: string; data: ReservationCreateResponse }>(
    "/api/reservations",
    { method: "POST", body: payload }
  );
  return res.data;
}

/** 예약 상세조회 */
export async function getReservationClient(reservationId: number) {
  const res = await apiFetch<{ message: string; data: ReservationDetailResponse }>(
    `/api/reservations/${reservationId}`
  );
  return res.data;
}

/** 내 예약 목록조회 */
export async function getMyReservationsClient(page: number = 0) {
  const res = await apiFetch<{ message: string; data: ReservationListResponse }>(
    `/api/reservations/me?page=${page}`
  );
  return res.data;
}

/** 캠핑장 상세조회 */
export async function getCampingDetailClient(campingId: number) {
  const res = await apiFetch<{ message: string; data: CampingDetail }>(
    `/api/campings/${campingId}`
  );
  return res.data;
}

/** 예약 취소 */
export async function cancelReservationClient(reservationId: number) {
  const res = await apiFetch<{ message: string; data: { id: number; rsvNum: string; status: string } }>(
    `/api/reservations/${reservationId}/cancel`,
    { method: "PATCH" }
  );
  return res.data;
}