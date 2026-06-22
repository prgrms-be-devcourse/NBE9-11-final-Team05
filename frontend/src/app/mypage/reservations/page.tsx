import Link from "next/link";
import { getMyReservations } from "@/lib/api/reservation";
import Card from "@/components/ui/Card";
import Badge from "@/components/ui/Badge";

interface PageProps {
  searchParams: Promise<{ page?: string }>;
}

const STATUS_BADGE: Record<
  string,
  { label: string; tone: "neutral" | "success" | "danger" }
> = {
  CONFIRMED: { label: "예약 완료", tone: "success" },
  CANCELLED: { label: "예약 취소됨", tone: "danger" },
  PENDING: { label: "결제 대기", tone: "neutral" },
};

export default async function MyReservationsPage({ searchParams }: PageProps) {
  const { page } = await searchParams;
  const reservations = await getMyReservations(Number(page ?? 0));

  // 응답이 배열인지 페이지네이션 객체인지 확인 후 처리
  const content = (Array.isArray(reservations) ? reservations : reservations?.content ?? [])
  .filter((r) => r.status !== "PENDING");

  

  return (
    <div className="mx-auto max-w-2xl p-6">
      <h1 className="mb-6 text-xl font-bold text-stone-900">예약 내역 조회</h1>

      {content.length === 0 && (
        <p className="py-16 text-center text-stone-400">예약 내역이 없습니다.</p>
      )}

      <ul className="flex flex-col gap-4">
        {content.map((reservation) => {
          const badge = STATUS_BADGE[reservation.status] ?? {
            label: reservation.status,
            tone: "neutral" as const,
          };
          return (
            <li key={reservation.id}>
              <Card className="flex gap-4 p-4">
                {reservation.imageUrl ? (
                  <img
                    src={reservation.imageUrl}
                    alt={reservation.campingName}
                    className="h-20 w-24 shrink-0 rounded-2xl object-cover"
                  />
                ) : (
                  <div className="h-20 w-24 shrink-0 rounded-2xl bg-stone-100" />
                )}

                <div className="flex flex-1 flex-col gap-1">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-semibold text-stone-900">
                      {reservation.campingName}
                    </h3>
                    <Badge tone={badge.tone}>{badge.label}</Badge>
                  </div>
                  <p className="text-sm text-stone-500">
                    {reservation.checkIn} - {reservation.checkOut}
                  </p>
                  <Link
                    href={`/mypage/reservations/${reservation.id}`}
                    className="mt-1 self-start rounded-full bg-amber-50 px-4 py-1.5 text-xs font-semibold text-amber-700 hover:bg-amber-100"
                  >
                    예약 상세
                  </Link>
                </div>
              </Card>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
