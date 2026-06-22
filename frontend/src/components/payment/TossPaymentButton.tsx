"use client";

import { useState } from "react";
import { loadTossPayments } from "@tosspayments/payment-sdk";
import { TOSS_CLIENT_KEY } from "@/lib/toss";
import { createPaymentClient } from "@/lib/api/payment.client";
import { ApiError } from "@/lib/api/core";
import Button from "@/components/ui/Button";

interface TossPaymentButtonProps {
  reservationId: number;
  /** 버튼에 보여줄 금액 (참고용 표시 — 실제 결제 금액은 결제 생성 응답값을 사용) */
  displayAmount: number;
  successUrl: string;
  failUrl: string;
}

/**
 * 클릭 시점에:
 * 1) 결제 생성(POST /api/payments) — 이 시점에 비로소 결제 DB에 PENDING row 생성
 * 2) 응답으로 받은 orderId/amount/orderName/customerName으로 토스 SDK 호출
 *
 * 따라서 사용자가 이 버튼을 누르기 전까지는 결제 레코드가 전혀 생기지 않는다.
 */
export default function TossPaymentButton({
  reservationId,
  displayAmount,
  successUrl,
  failUrl,
}: TossPaymentButtonProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleClick = async () => {
    setError(null);
    setIsLoading(true);
    try {
      // 1) 결제 생성 — 이 순간 결제 DB row 생성
      const payment = await createPaymentClient({ reservationId });

      // 2) 토스 SDK 호출 — 브라우저가 결제창으로 이동하므로 이후 코드는 실행되지 않는다.
      const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY);
      await tossPayments.requestPayment("카드", {
        amount: payment.amount,
        orderId: payment.orderId,
        orderName: payment.orderName,
        customerName: payment.customerName,
        successUrl,
        failUrl,
      });
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : err instanceof Error
            ? err.message
            : "결제 요청 중 문제가 발생했습니다.";
      setError(message);
      setIsLoading(false);
    }
  };

  return (
    <div>
      <Button onClick={handleClick} disabled={isLoading} fullWidth>
        {isLoading
          ? "결제창 여는 중..."
          : `${displayAmount.toLocaleString()}원 결제하기`}
      </Button>
      {error && (
        <p className="mt-2 rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">
          {error}
        </p>
      )}
    </div>
  );
}
