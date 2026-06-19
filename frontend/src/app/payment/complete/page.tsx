import PaymentConfirmer from "@/components/payment/PaymentConfirmer";

interface PageProps {
  searchParams: Promise<{
    orderId?: string;
    paymentKey?: string;
    amount?: string;
  }>;
}

export default async function PaymentCompletePage({ searchParams }: PageProps) {
  const { orderId, paymentKey, amount } = await searchParams;

  if (!orderId || !paymentKey || !amount) {
    return <p>결제 정보가 올바르지 않습니다.</p>;
  }

  // confirm 호출 자체는 클라이언트에서 수행 (쿠키 인증 + 결과에 따른 UI 분기 필요)
  return <PaymentConfirmer orderId={orderId} paymentKey={paymentKey} amount={amount} />;
}
