"use client";

import { useState } from "react";
import { loadTossPayments } from "@tosspayments/payment-sdk";
import { TOSS_CLIENT_KEY } from "@/lib/toss";
import Button from "@/components/ui/Button";
import type { PaymentResponse } from "@/types/payment";

interface TossPaymentButtonProps {
  payment: PaymentResponse;
  /** 결제 성공 후 토스가 리다이렉트할 경로. 쿼리에 orderId/paymentKey/amount가 자동으로 붙는다. */
  successUrl: string;
  /** 결제 실패/취소 시 리다이렉트할 경로. */
  failUrl: string;
}

/**
 * 예약 상세 페이지 등에서 이 컴포넌트만 클라이언트 컴포넌트로 끼워 넣으면 된다.
 * 부모 페이지는 서버 컴포넌트로 유지 가능.
 *
 * <TossPaymentButton
 *   payment={payment}
 *   successUrl={`${origin}/payment/complete`}
 *   failUrl={`${origin}/payment/fail`}
 * />
 */
export default function TossPaymentButton({
  payment,
  successUrl,
  failUrl,
}: TossPaymentButtonProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleClick = async () => {
    setError(null);
    setIsLoading(true);
    try {
      const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY);

      // requestPayment 호출 시점에 브라우저가 토스 결제창으로 이동하므로,
      // 이 이후 코드는 실행되지 않는다 (성공/실패는 successUrl/failUrl에서 처리).
      await tossPayments.requestPayment("카드", {
        amount: payment.amount,
        orderId: payment.orderId,
        orderName: payment.orderName,
        customerName: payment.customerName,
        successUrl,
        failUrl,
      });
    } catch (err) {
      // 사용자가 결제창을 직접 닫는 경우도 여기로 들어온다 (code: "USER_CANCEL")
      const message =
        err instanceof Error ? err.message : "결제 요청 중 문제가 발생했습니다.";
      setError(message);
      setIsLoading(false);
    }
  };

  return (
    <div>
      <Button onClick={handleClick} disabled={isLoading} fullWidth>
        {isLoading
          ? "결제창 여는 중..."
          : `${payment.amount.toLocaleString()}원 결제하기`}
      </Button>
      {error && (
        <p className="mt-2 rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">
          {error}
        </p>
      )}
    </div>
  );
}
