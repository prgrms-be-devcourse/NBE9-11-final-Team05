// 결제 생성 요청
export interface CreatePaymentRequest {
  reservationId: number;
}

// 결제 생성 응답
export interface PaymentResponse {
  orderId: string;
  orderName: string;
  amount: number;
  customerName: string;
}

// 결제 승인 요청
export interface ConfirmPaymentRequest {
  orderId: string;
  paymentKey: string;
  amount: string;
}

// 결제 승인 응답 — 백엔드 실제 응답 기준
export interface PaymentConfirmResponse {
  amount: number;
  approvedAt: string;
  campingName: string;
  checkIn: string;
  checkOut: string;
  guestCount: number;
  nights: number;
  orderId: string;
  paymentMethod: string;
  rsvName: string;
  rsvNo: string;
  rsvPhone: string;
  siteName: string;
}

// 결제 상태값
export type PaymentStatus = "READY" | "DONE" | "CANCELED" | "FAILED";

// 결제완료조회 응답
export interface PaymentDetailResponse {
  id: number;
  orderId: string;
  paymentKey: string;
  orderName: string;
  amount: number;
  status: PaymentStatus;
  approvedAt?: string;
}