"use server";

import { cancelReservation } from "@/lib/api/reservation";
import { ApiError } from "@/lib/api/core";

export interface CancelReservationState {
  success?: boolean;
  error?: string;
}

export async function cancelReservationAction(
  reservationId: number,
  _prevState: CancelReservationState
): Promise<CancelReservationState> {
  try {
    await cancelReservation(reservationId);
    return { success: true };
  } catch (err) {
    if (err instanceof ApiError) {
      return { error: err.message };
    }
    return { error: "예약 취소 중 오류가 발생했습니다." };
  }
}