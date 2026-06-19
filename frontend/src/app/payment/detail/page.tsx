import { getPayment } from "@/lib/api/payment";

interface PageProps {
  searchParams: Promise<{ paymentId?: string }>;
}

export default async function PaymentDetailPage({ searchParams }: PageProps) {
  const { paymentId } = await searchParams;

  if (!paymentId) {
    return <p>조회할 결제 정보가 없습니다.</p>;
  }

  const payment = await getPayment(Number(paymentId));

  return (
    <div>
      <h1>결제 상세</h1>
      <dl>
        <dt>주문번호</dt>
        <dd>{payment.orderId}</dd>
        <dt>상품명</dt>
        <dd>{payment.orderName}</dd>
        <dt>결제금액</dt>
        <dd>{payment.amount.toLocaleString()}원</dd>
        <dt>상태</dt>
        <dd>{payment.status}</dd>
      </dl>
    </div>
  );
}
