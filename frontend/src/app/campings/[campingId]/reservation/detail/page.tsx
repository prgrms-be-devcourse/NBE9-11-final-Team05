import { getReservation } from "@/lib/api/reservation";
import { createPayment } from "@/lib/api/payment";
import TossPaymentButton from "@/components/payment/TossPaymentButton";
import Card from "@/components/ui/Card";
import DataRow from "@/components/ui/DataRow";

interface PageProps {
  params: Promise<{ campingId: string }>;
  searchParams: Promise<{ reservationId?: string }>;
}

export default async function ReservationDetailPage({ searchParams }: PageProps) {
  const { reservationId } = await searchParams;

  if (!reservationId) {
    return <p className="p-10 text-center text-stone-500">예약 정보를 찾을 수 없습니다.</p>;
  }

  const reservation = await getReservation(Number(reservationId));
  const payment = await createPayment({ reservationId: reservation.id });

  const baseUrl = process.env.NEXT_PUBLIC_BASE_URL ?? "http://localhost:3000";

  return (
    <div className="mx-auto grid max-w-4xl grid-cols-1 gap-6 p-6 md:grid-cols-2">
      {/* 좌측: 결제 진행 */}
      <Card>
        <h2 className="mb-5 text-lg font-bold text-stone-900">결제 진행</h2>
        <p className="mb-3 font-semibold text-stone-900">{reservation.campingName}</p>
        <div className="divide-y divide-stone-100">
          <DataRow label="예약자 성함">{reservation.rsvName}</DataRow>
          <DataRow label="예약일">{reservation.checkIn} - {reservation.checkOut}</DataRow>
          <DataRow label="예약인원">{reservation.guestCount}명</DataRow>
          <DataRow label="예약구역">{reservation.siteName}</DataRow>
        </div>
        <div className="mt-5 flex items-baseline justify-between border-t border-stone-100 pt-4">
          <span className="text-sm text-stone-500">총 결제 금액</span>
          <span className="text-xl font-bold text-stone-900">
            {payment.amount.toLocaleString()}원
          </span>
        </div>
      </Card>

      {/* 우측: 결제 수단 */}
      <Card className="flex flex-col">
        <h2 className="mb-5 text-lg font-bold text-stone-900">결제 수단</h2>
        <p className="mb-6 text-sm text-stone-500">
          아래 버튼을 누르면 토스페이먼츠 결제창이 열립니다.
        </p>
        <div className="mt-auto">
          <TossPaymentButton
            payment={payment}
            successUrl={`${baseUrl}/payment/complete`}
            failUrl={`${baseUrl}/payment/fail`}
          />
        </div>
      </Card>
    </div>
  );
}
