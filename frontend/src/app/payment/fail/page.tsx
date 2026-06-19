import Link from "next/link";

interface PageProps {
  // 토스가 failUrl 리다이렉트 시 실제로 붙여주는 쿼리 파라미터
  searchParams: Promise<{
    code?: string;
    message?: string;
    orderId?: string;
  }>;
}

export default async function PaymentFailPage({ searchParams }: PageProps) {
  const { code, message, orderId } = await searchParams;

  return (
    <div>
      <h1>결제에 실패했습니다</h1>
      <p>{message ?? "결제가 정상적으로 처리되지 않았습니다."}</p>
      {code && <p>에러코드: {code}</p>}
      {orderId && <p>주문번호: {orderId}</p>}

      <Link href="/mypage/reservations">예약 내역으로 이동</Link>
    </div>
  );
}
