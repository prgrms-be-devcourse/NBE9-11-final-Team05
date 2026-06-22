import { apiFetch } from "./core";
import type {
  ConfirmPaymentRequest,
  CreatePaymentRequest,
  PaymentConfirmResponse,
  PaymentResponse,
} from "@/types/payment";

/**
 * 결제 생성 — 사용자가 "토스로 결제하기" 버튼을 누르는 시점에 클라이언트에서 호출.
 * 이 시점에 비로소 결제 DB에 PENDING row가 생긴다.
 * (이전에는 결제상세 페이지 진입 시 서버 컴포넌트에서 자동 호출했으나,
 *  사용자가 페이지만 보고 이탈해도 결제 레코드가 생기는 문제가 있어 변경)
 */
export async function createPaymentClient(payload: CreatePaymentRequest) {
  const res = await apiFetch<{ message: string; data: PaymentResponse }>(
    "/api/payments",
    {
      method: "POST",
      body: payload,
    }
  );
  return res.data;
}

/**
 * 결제 승인 — 토스 successUrl로 돌아온 직후 클라이언트에서 호출.
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