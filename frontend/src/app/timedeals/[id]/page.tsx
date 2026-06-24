// app/timedeals/[id]/page.tsx

import Link from "next/link";
import { notFound } from "next/navigation";
import { getTimeDeal } from "@/lib/api/timedeal";
import { canReserve } from "@/lib/utils/auth";
import TimeDealReservationCard from "@/components/timedeal/TimeDealReservationCard";

interface Props {
  params: Promise<{ id: string }>;
}

function getTimeLeft(saleEndAt: string): string {
  const diff = new Date(saleEndAt).getTime() - Date.now();
  if (diff <= 0) return "마감";
  const h = Math.floor(diff / 1000 / 60 / 60);
  const m = Math.floor((diff / 1000 / 60) % 60);
  if (h >= 24) return `${Math.floor(h / 24)}일 후 마감`;
  if (h > 0) return `${h}시간 ${m}분 남음`;
  return `${m}분 남음`;
}

const STATUS_LABEL: Record<string, { text: string; color: string }> = {
  SCHEDULED: { text: "판매 예정", color: "bg-amber-50 text-amber-600 border border-amber-200" },
  ACTIVE:    { text: "판매 중",  color: "bg-green-50 text-[#4B6945] border border-green-200" },
  SOLD_OUT:  { text: "매진",    color: "bg-gray-100 text-gray-400 border border-gray-200" },
  ENDED:     { text: "종료",    color: "bg-gray-100 text-gray-400 border border-gray-200" },
  CANCELLED: { text: "취소됨",  color: "bg-gray-100 text-gray-400 border border-gray-200" },
};

export default async function TimeDealDetailPage({ params }: Props) {
  const { id } = await params;
  const timeDealId = Number(id);

  if (isNaN(timeDealId)) notFound();

  const [deal, reservable] = await Promise.all([
    getTimeDeal(timeDealId).catch(() => null),
    canReserve(),
  ]);

  if (!deal) notFound();

  const nights = Math.round(
    (new Date(deal.checkOut).getTime() - new Date(deal.checkIn).getTime()) /
      (1000 * 60 * 60 * 24)
  );
  const status = STATUS_LABEL[deal.status] ?? STATUS_LABEL.ENDED;
  const soldPct = Math.min(100, Math.round((deal.soldCount / deal.quantity) * 100));

  return (
    <div className="max-w-7xl mx-auto px-6 py-10">

      {/* 브레드크럼 */}
      <nav className="flex items-center gap-2 text-sm text-gray-400 mb-8">
        <Link href="/" className="hover:text-gray-600">홈</Link>
        <span>/</span>
        <Link href={`/campings/${deal.campingId}`} className="hover:text-gray-600">
          {deal.campingName}
        </Link>
        <span>/</span>
        <span className="text-gray-700 font-medium">타임딜</span>
      </nav>

      <div className="grid lg:grid-cols-[2fr_1fr] gap-12">

        {/* 왼쪽 — 타임딜 상세 정보 */}
        <div className="space-y-8">

          {/* 헤더 */}
          <div>
            <div className="flex items-center gap-3 mb-3">
              <span className={`px-3 py-1 rounded-full text-xs font-semibold ${status.color}`}>
                {status.text}
              </span>
              {deal.status === "ACTIVE" && (
                <span className="text-sm text-red-500 font-medium">
                  🔥 {getTimeLeft(deal.saleEndAt)}
                </span>
              )}
            </div>
            <h1 className="text-3xl font-bold">{deal.campingName}</h1>
            <p className="text-gray-500 mt-1 text-lg">{deal.siteName}</p>
          </div>

          {/* 가격 카드 */}
          <div className="bg-[#F6F8F4] rounded-3xl p-6">
            <p className="text-sm text-gray-400 mb-2">타임딜 특가</p>
            <div className="flex items-baseline gap-3 mb-1">
              <span className="text-4xl font-bold text-[#4B6945]">
                {deal.dealPrice.toLocaleString("ko-KR")}원
              </span>
              <span className="text-lg text-gray-300 line-through">
                {deal.originalPrice.toLocaleString("ko-KR")}원
              </span>
              <span className="text-xl font-bold text-amber-500">{deal.discountRate}% 할인</span>
            </div>
            <p className="text-sm text-gray-400">1박 기준 · {nights}박 총 {(deal.dealPrice * nights).toLocaleString()}원</p>
          </div>

          {/* 예약 정보 */}
          <div>
            <h2 className="text-xl font-bold mb-4">예약 정보</h2>
            <div className="grid grid-cols-2 gap-4">
              {[
                { label: "체크인", value: deal.checkIn },
                { label: "체크아웃", value: deal.checkOut },
                { label: "숙박", value: `${nights}박` },
                { label: "캠핑장", value: deal.campingName },
                { label: "구역", value: deal.siteName },
              ].map(({ label, value }) => (
                <div key={label} className="bg-white rounded-2xl border border-gray-100 px-5 py-4">
                  <p className="text-xs text-gray-400 mb-1">{label}</p>
                  <p className="font-semibold text-gray-800">{value}</p>
                </div>
              ))}
            </div>
          </div>

          {/* 판매 기간 */}
          <div>
            <h2 className="text-xl font-bold mb-4">판매 기간</h2>
            <div className="bg-white rounded-2xl border border-gray-100 px-5 py-4 space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-400">판매 시작</span>
                <span className="font-medium">{deal.saleStartAt.replace("T", " ").substring(0, 16)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">판매 종료</span>
                <span className="font-medium">{deal.saleEndAt.replace("T", " ").substring(0, 16)}</span>
              </div>
            </div>
          </div>

          {/* 재고 현황 */}
          <div>
            <h2 className="text-xl font-bold mb-4">예약 현황</h2>
            <div className="bg-white rounded-2xl border border-gray-100 px-5 py-4">
              <div className="flex justify-between text-sm mb-2">
                <span className="text-gray-500">판매 수량</span>
                <span>
                  <span className="font-bold text-gray-800">{deal.soldCount}</span>
                  <span className="text-gray-400"> / {deal.quantity}개</span>
                </span>
              </div>
              <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all ${
                    soldPct >= 100 ? "bg-gray-300" :
                    soldPct >= 70 ? "bg-amber-400" : "bg-[#4B6945]"
                  }`}
                  style={{ width: `${soldPct}%` }}
                />
              </div>
              <p className="text-xs text-gray-400 mt-2">
                {deal.remaining > 0
                  ? `${deal.remaining}개 남았습니다`
                  : "매진되었습니다"}
              </p>
            </div>
          </div>

          {/* 캠핑장 상세 링크 */}
          <Link
            href={`/campings/${deal.campingId}`}
            className="inline-flex items-center gap-1 text-sm text-[#4B6945] font-medium hover:underline"
          >
            캠핑장 상세 정보 보기 →
          </Link>
        </div>

        {/* 오른쪽 — 예약 카드 */}
        {reservable ? (
          <TimeDealReservationCard deal={deal} />
        ) : (
          <aside className="sticky top-24">
            <div className="bg-white rounded-3xl shadow-lg p-6 border border-gray-100 text-center">
              <p className="text-gray-400 text-sm mb-3">예약하려면 로그인이 필요합니다</p>
              <Link
                href="/login"
                className="inline-block px-6 py-2.5 bg-[#4B6945] text-white text-sm font-semibold rounded-2xl hover:bg-[#3d5738] transition"
              >
                로그인하기
              </Link>
            </div>
          </aside>
        )}
      </div>
    </div>
  );
}