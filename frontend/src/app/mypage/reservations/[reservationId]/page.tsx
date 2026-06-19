import { getReservation } from "@/lib/api/reservation";
import CancelReservationButton from "@/components/reservation/CancelReservationButton";
import Card from "@/components/ui/Card";
import DataRow from "@/components/ui/DataRow";
import Button from "@/components/ui/Button";

interface PageProps {
  params: Promise<{ reservationId: string }>;
}

export default async function MyReservationDetailPage({ params }: PageProps) {
  const { reservationId } = await params;
  const reservation = await getReservation(Number(reservationId));

  return (
    <div className="mx-auto max-w-md p-6">
      <Card>
        {reservation.imageUrl ? (
          <img
            src={reservation.imageUrl}
            alt={reservation.campingName}
            className="mb-4 h-40 w-full rounded-2xl object-cover"
          />
        ) : (
          <div className="mb-4 h-40 w-full rounded-2xl bg-stone-100" />
        )}

        <h2 className="mb-1 text-lg font-bold text-stone-900">
          {reservation.campingName}
        </h2>
        <p className="mb-4 text-xs text-stone-400">예약번호: {reservation.rsvNum}</p>

        <div className="divide-y divide-stone-100">
          <DataRow label="예약자">{reservation.rsvName}</DataRow>
          <DataRow label="예약일">{reservation.checkIn} - {reservation.checkOut}</DataRow>
          <DataRow label="구역">{reservation.siteName}</DataRow>
          <DataRow label="예약 인원">{reservation.guestCount}명</DataRow>
          <DataRow label="예약자 번호">{reservation.rsvPhone}</DataRow>
          <DataRow label="요청 사항">{reservation.request ?? "-"}</DataRow>
        </div>

        <div className="mt-4 flex items-baseline justify-between border-t border-stone-100 pt-4">
          <span className="text-sm text-stone-500">결제금액</span>
          <span className="text-lg font-bold text-stone-900">
            {reservation.rsvPrice.toLocaleString()}원
          </span>
        </div>
      </Card>

      <div className="mt-6 flex flex-col gap-3">
        <Button variant="ghost" fullWidth>
          캠핑장 상세 페이지로 이동
        </Button>
        <CancelReservationButton />
      </div>
    </div>
  );
}
