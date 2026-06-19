import { apiFetch } from "./core";
import type { ConfirmPaymentRequest, PaymentConfirmResponse } from "@/types/payment";

/**
 * 결제 승인 — 토스 successUrl로 돌아온 직후 클라이언트에서 호출.
 * 백엔드 응답이 { message, data } 형태로 감싸져 있으므로 data를 꺼내서 반환한다.
 */
export async function confirmPayment(payload: ConfirmPaymentRequest) {
  const res = await apiFetch<{ message: string; data: PaymentConfirmResponse }>(
    "/api/payments/confirm",
    {
      method: "POST",
      body: payload,
    }
  );
  return res.data;
}