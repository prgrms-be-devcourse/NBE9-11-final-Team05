import "server-only";
import { serverApiFetch } from "./server";
import type {
  CreateReservationRequest,
  ReservationCreateResponse,
  ReservationDetailResponse,
  ReservationListResponse,
} from "@/types/reservation";
import { CampingDetail } from "@/types/camping";


/** 예약 생성 */
export function createReservation(payload: CreateReservationRequest) {
  return serverApiFetch<ReservationCreateResponse>("/api/reservations", {
    method: "POST",
    body: payload,
  });
}

/** 예약 상세조회 (서버 컴포넌트) */
export function getReservation(reservationId: number) {
  return serverApiFetch<ReservationDetailResponse>(
    `/api/reservations/${reservationId}`
  );
}

/** 내 예약 목록조회 */
export function getMyReservations(page: number = 0) {
  return serverApiFetch<ReservationListResponse>(
    `/api/reservations/me?page=${page}`
  );
}

/** 캠핑장 상세조회 — 구역(site) 목록 가져올 때 사용 */
export function getCampingDetail(campingId: number) {
  return serverApiFetch<CampingDetail>(`/api/campings/${campingId}`);
}