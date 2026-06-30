import Link from "next/link";
import { getActiveTimeDeals, TimeDealResponse } from "../../lib/api/timedeal";

function getTimeLeft(saleEndAt: string): string {
  const diff = new Date(saleEndAt).getTime() - Date.now();
  if (diff <= 0) return "마감";
  const h = Math.floor(diff / 1000 / 60 / 60);
  const m = Math.floor((diff / 1000 / 60) % 60);
  if (h >= 24) return `${Math.floor(h / 24)}일 후 마감`;
  if (h > 0) return `${h}시간 ${m}분 남음`;
  return `${m}분 남음`;
}

type SortKey = "latest" | "discount" | "price" | "deadline";

const SORT_OPTIONS: { label: string; value: SortKey }[] = [
  { label: "최신순", value: "latest" },
  { label: "할인율순", value: "discount" },
  { label: "낮은 가격순", value: "price" },
  { label: "마감 임박순", value: "deadline" },
];

function sortDeals(deals: TimeDealResponse[], sort: SortKey): TimeDealResponse[] {
  const active = deals.filter((d) => d.status !== "SOLD_OUT" && d.remaining > 0);
  const soldOut = deals.filter((d) => d.status === "SOLD_OUT" || d.remaining === 0);

  const sorted = [...active].sort((a, b) => {
    switch (sort) {
      case "discount":
        return b.discountRate - a.discountRate;
      case "price":
        return a.dealPrice - b.dealPrice;
      case "deadline":
        return new Date(a.saleEndAt).getTime() - new Date(b.saleEndAt).getTime();
      default: // latest
        return b.id - a.id;
    }
  });

  return [...sorted, ...soldOut];
}

function TimeDealCard({ deal }: { deal: TimeDealResponse }) {
  const isSoldOut = deal.status === "SOLD_OUT" || deal.remaining === 0;
  const soldPct = Math.min(100, Math.round((deal.soldCount / deal.quantity) * 100));
  const timeLeft = getTimeLeft(deal.saleEndAt);
  const isUrgent = !isSoldOut && new Date(deal.saleEndAt).getTime() - Date.now() < 3 * 60 * 60 * 1000; // 3시간 이내

  return (
    <Link href={`/timedeals/${deal.id}`}>
      <div
        className={`bg-white rounded-3xl shadow-md p-5 transition cursor-pointer flex flex-col h-full
          ${isSoldOut ? "opacity-60" : "hover:shadow-xl hover:-translate-y-0.5"}
          ${isUrgent ? "ring-2 ring-red-300" : ""}
        `}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-2">
          <div className="min-w-0">
            <p className="text-xs text-gray-400 truncate">{deal.campingName}</p>
            <p className="font-bold text-base truncate">{deal.siteName}</p>
          </div>
          {isSoldOut ? (
            <span className="shrink-0 ml-2 px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-400">
              매진
            </span>
          ) : (
            <span
              className={`shrink-0 ml-2 px-2.5 py-1 rounded-full text-xs font-semibold
                ${isUrgent ? "bg-red-500 text-white animate-pulse" : "bg-red-50 text-red-500"}`}
            >
              🔥 {timeLeft}
            </span>
          )}
        </div>

        {/* 날짜 */}
        <p className="text-sm text-gray-400 mb-4">
          {deal.checkIn} ~ {deal.checkOut}
        </p>

        {/* 가격 */}
        <div className="flex items-baseline gap-2 mb-3 mt-auto">
          <span className="text-2xl font-bold text-[#4B6945]">
            {deal.dealPrice.toLocaleString("ko-KR")}원
          </span>
          <span className="text-sm text-gray-300 line-through">
            {deal.originalPrice.toLocaleString("ko-KR")}원
          </span>
          <span className="text-sm font-bold text-amber-500">{deal.discountRate}%↓</span>
        </div>

        {/* 수량 바 */}
        <div>
          <div className="flex justify-between text-xs text-gray-400 mb-1">
            <span>남은 수량</span>
            <span>
              <span className="font-semibold text-gray-600">{deal.remaining}</span>/{deal.quantity}개
            </span>
          </div>
          <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full transition-all
                ${soldPct >= 100 ? "bg-gray-300" : soldPct >= 70 ? "bg-amber-400" : "bg-[#4B6945]"}`}
              style={{ width: `${soldPct}%` }}
            />
          </div>
        </div>
      </div>
    </Link>
  );
}

export default async function TimeDealsPage({
  searchParams,
}: {
  searchParams: { sort?: SortKey };
}) {
  const sort: SortKey = searchParams.sort ?? "latest";
  const allDeals = await getActiveTimeDeals();
  const deals = sortDeals(allDeals, sort);

  const activeCount = allDeals.filter((d) => d.status !== "SOLD_OUT" && d.remaining > 0).length;
  const soldOutCount = allDeals.length - activeCount;

  return (
    <div className="max-w-6xl mx-auto">
      {/* 페이지 헤더 */}
      <div className="mb-10">
        <Link href="/" className="text-sm text-gray-400 hover:text-gray-600 mb-4 inline-block">
          ← 홈으로
        </Link>
        <div className="flex items-end justify-between flex-wrap gap-4">
          <div>
            <p className="text-sm font-semibold text-red-500 mb-1">⚡ 한정 특가</p>
            <h1 className="text-4xl font-bold">타임딜 전체보기</h1>
            <p className="text-gray-400 mt-2 text-sm">
              진행중{" "}
              <span className="font-semibold text-[#4B6945]">{activeCount}개</span>
              {soldOutCount > 0 && (
                <>
                  {" "}· 매진{" "}
                  <span className="font-semibold text-gray-400">{soldOutCount}개</span>
                </>
              )}
            </p>
          </div>

          {/* 정렬 탭 */}
          <div className="flex gap-2 flex-wrap">
            {SORT_OPTIONS.map((opt) => (
              <Link key={opt.value} href={`/timedeals?sort=${opt.value}`}>
                <button
                  className={`px-4 py-2 rounded-full text-sm font-semibold transition
                    ${sort === opt.value
                      ? "bg-[#4B6945] text-white"
                      : "bg-gray-100 text-gray-500 hover:bg-gray-200"
                    }`}
                >
                  {opt.label}
                </button>
              </Link>
            ))}
          </div>
        </div>
      </div>

      {/* 딜 없을 때 */}
      {deals.length === 0 ? (
        <div className="text-center py-32 text-gray-400">
          <p className="text-5xl mb-4">🏕️</p>
          <p className="text-lg font-semibold">현재 진행 중인 타임딜이 없습니다.</p>
          <p className="text-sm mt-2">곧 새로운 특가가 올라올 예정이에요!</p>
          <Link href="/campings">
            <button className="mt-8 bg-[#4B6945] text-white px-6 py-3 rounded-full text-sm font-semibold hover:opacity-90">
              캠핑장 둘러보기
            </button>
          </Link>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {deals.map((deal) => (
            <TimeDealCard key={deal.id} deal={deal} />
          ))}
        </div>
      )}
    </div>
  );
}