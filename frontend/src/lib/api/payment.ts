
import "server-only";
import { serverApiFetch } from "./server";
import type { PaymentDetailResponse } from "@/types/payment";
 
// 결제 생성(createPayment)은 더 이상 서버 컴포넌트에서 호출하지 않는다.
// "토스로 결제하기" 버튼을 누른 시점에만 결제 레코드가 생성되어야 하므로,
// lib/api/payment.client.ts의 createPaymentClient를 사용한다.
 
/** 결제완료조회 (서버 컴포넌트) */
export function getPayment(paymentId: number) {
  return serverApiFetch<PaymentDetailResponse>(`/api/payments/${paymentId}`);
}
 