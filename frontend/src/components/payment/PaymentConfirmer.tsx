"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { confirmPayment } from "@/lib/api/payment.client";
import { ApiError } from "@/lib/api/core";
import Card from "@/components/ui/Card";
import Button from "@/components/ui/Button";
import DataRow from "@/components/ui/DataRow";
import type { PaymentConfirmResponse } from "@/types/payment";

interface PaymentConfirmerProps {
  orderId: string;
  paymentKey: string;
  amount: string;
}

type ConfirmState =
  | { status: "loading" }
  | { status: "success"; data: PaymentConfirmResponse }
  | { status: "error"; message: string };

export default function PaymentConfirmer({
  orderId,
  paymentKey,
  amount,
}: PaymentConfirmerProps) {
  const [state, setState] = useState<ConfirmState>({ status: "loading" });

  useEffect(() => {
    let isMounted = true;

    (async () => {
      try {
        const data = await confirmPayment({ orderId, paymentKey, amount });
        if (isMounted) setState({ status: "success", data });
      } catch (err) {
        if (!isMounted) return;
        const message =
          err instanceof ApiError
            ? err.message
            : "결제 승인 처리 중 문제가 발생했습니다.";
        setState({ status: "error", message });
      }
    })();

    return () => {
      isMounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (state.status === "loading") {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <p className="text-stone-500">결제를 확인하고 있습니다...</p>
      </div>
    );
  }

  if (state.status === "error") {
    return (
      <div className="mx-auto max-w-md p-6 text-center">
        <h1 className="mb-3 text-xl font-bold text-stone-900">결제 승인 실패</h1>
        <p className="mb-6 text-sm text-stone-500">{state.message}</p>
        <Link href="/mypage">
          <Button variant="secondary" fullWidth>마이페이지로 이동</Button>
        </Link>
      </div>
    );
  }

  const { data } = state;

  return (
    <div className="mx-auto max-w-md p-6">
      <h1 className="mb-6 text-center text-xl font-bold text-stone-900">
        예약 및 결제가 완료되었습니다!
      </h1>

      <Card>
        <div className="mb-4 h-32 w-full rounded-2xl bg-stone-100" />

        <p className="mb-1 text-xs text-stone-400">예약번호: {data.rsvNo}</p>
        <p className="mb-3 font-semibold text-stone-900">{data.campingName}</p>

        <div className="divide-y divide-stone-100">
          <DataRow label="예약자 성함">{data.rsvName}</DataRow>
          <DataRow label="예약일">{data.checkIn} - {data.checkOut}</DataRow>
          <DataRow label="예약인원">{data.guestCount}명</DataRow>
          <DataRow label="예약구역">{data.siteName}</DataRow>
          <DataRow label="결제수단">{data.paymentMethod}</DataRow>
          <DataRow label="결제금액">{data.amount.toLocaleString()}원</DataRow>
        </div>
      </Card>

      <div className="mt-6 flex flex-col gap-3">
        <Link href="/mypage/reservations">
          <Button fullWidth>예약 상세보기 이동</Button>
        </Link>
        <Link href="/">
          <Button variant="secondary" fullWidth>메인으로 이동</Button>
        </Link>
      </div>
    </div>
  );
}
