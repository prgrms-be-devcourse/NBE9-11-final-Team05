import { serverApiFetch } from "./server";
import { apiFetch } from "./core";
import type {
  ConfirmPaymentRequest,
  CreatePaymentRequest,
  PaymentDetailResponse,
  PaymentResponse,
} from "@/types/payment";

/** 결제 생성 — 예약 페이지(서버 컴포넌트/서버 액션)에서 호출 */
export function createPayment(payload: CreatePaymentRequest) {
  return serverApiFetch<PaymentResponse>("/api/payments", {
    method: "POST",
    body: payload,
  });
}

/**
 * 결제 승인 — 토스 successUrl로 돌아온 직후 "클라이언트"에서 호출.
 * 이 시점은 브라우저 리다이렉트 직후라 쿠키가 자동으로 같이 전송된다
 * (credentials: "include", apiFetch 기본 설정).
 */
export function confirmPayment(payload: ConfirmPaymentRequest) {
  return apiFetch<PaymentDetailResponse>("/api/payments/confirm", {
    method: "POST",
    body: payload,
  });
}

/** 결제완료조회 — 서버 컴포넌트에서 호출 */
export function getPayment(paymentId: number) {
  return serverApiFetch<PaymentDetailResponse>(`/api/payments/${paymentId}`);
}