// app/page.tsx
// TimeDealCard의 Link href만 /campings/${deal.campingId} → /timedeals/${deal.id} 로 변경

import Link from "next/link";
import { getLatestCampings } from "../lib/api/camping";
import { getActiveTimeDeals, TimeDealResponse } from "../lib/api/timedeal";
import SearchBox from "../components/search/SearchBox";

function getTimeLeft(saleEndAt: string): string {
  const diff = new Date(saleEndAt).getTime() - Date.now();
  if (diff <= 0) return "마감";
  const h = Math.floor(diff / 1000 / 60 / 60);
  const m = Math.floor((diff / 1000 / 60) % 60);
  if (h >= 24) return `${Math.floor(h / 24)}일 후 마감`;
  if (h > 0) return `${h}시간 ${m}분 남음`;
  return `${m}분 남음`;
}

function TimeDealCard({ deal }: { deal: TimeDealResponse }) {
  const isSoldOut = deal.status === "SOLD_OUT" || deal.remaining === 0;
  const soldPct = Math.min(100, Math.round((deal.soldCount / deal.quantity) * 100));
  const timeLeft = getTimeLeft(deal.saleEndAt);

  return (
    // ✅ /campings/${deal.campingId} → /timedeals/${deal.id} 로 변경
    <Link href={`/timedeals/${deal.id}`}>
      <div className={`bg-white rounded-3xl shadow-lg p-5 transition cursor-pointer
        ${isSoldOut ? "opacity-60" : "hover:shadow-xl"}`}
      >
        <div className="flex items-start justify-between mb-3">
          <div className="min-w-0">
            <p className="text-xs text-gray-400 truncate">{deal.campingName}</p>
            <p className="font-bold text-base truncate">{deal.siteName}</p>
          </div>
          {isSoldOut ? (
            <span className="shrink-0 ml-2 px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-400">
              매진
            </span>
          ) : (
            <span className="shrink-0 ml-2 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-50 text-red-500">
              🔥 {timeLeft}
            </span>
          )}
        </div>

        <p className="text-sm text-gray-400 mb-4">{deal.checkIn} ~ {deal.checkOut}</p>

        <div className="flex items-baseline gap-2 mb-3">
          <span className="text-2xl font-bold text-[#4B6945]">
            {deal.dealPrice.toLocaleString("ko-KR")}원
          </span>
          <span className="text-sm text-gray-300 line-through">
            {deal.originalPrice.toLocaleString("ko-KR")}원
          </span>
          <span className="text-sm font-bold text-amber-500">{deal.discountRate}%↓</span>
        </div>

        <div>
          <div className="flex justify-between text-xs text-gray-400 mb-1">
            <span>남은 수량</span>
            <span><span className="font-semibold text-gray-600">{deal.remaining}</span>/{deal.quantity}개</span>
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

export default async function Home() {
  const [campings, timeDeals] = await Promise.all([
    getLatestCampings(),
    getActiveTimeDeals(),
  ]);

  return (
    <>
      <section className="relative overflow-hidden rounded-[32px] bg-[#F6F8F4] py-16">
        <div className="absolute -top-20 -left-20 w-72 h-72 rounded-full bg-[#4B6945]/10 blur-3xl" />
        <div className="relative max-w-4xl mx-auto text-center">
          <p className="text-[#4B6945] font-semibold mb-3">국내 캠핑장 통합 검색</p>
          <h1 className="text-4xl font-bold leading-tight">
            자연 속에서<br />특별한 하루를 만나보세요
          </h1>
          <p className="text-gray-500 mt-5 text-base">전국 캠핑장을 한 곳에서 검색하고 예약하세요.</p>
          <div className="bg-white rounded-3xl shadow-lg mt-10 max-w-3xl mx-auto p-2">
            <SearchBox />
          </div>
          <div className="flex justify-center gap-3 mt-8 flex-wrap">
            {["강원도", "경기도", "제주도", "부산", "충청도"].map((region) => (
              <Link key={region} href={`/campings?keyword=${region}`}>
                <button className="bg-[#4B6945] text-white px-4 py-2 rounded-full text-sm hover:opacity-90">
                  {region}
                </button>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {timeDeals.length > 0 && (
        <section className="mt-20">
          <div className="flex items-end justify-between mb-8">
            <div>
              <p className="text-sm font-semibold text-red-500 mb-1">⚡ 한정 특가</p>
              <h2 className="text-3xl font-bold">지금 바로 예약하세요</h2>
              <p className="text-gray-400 mt-1 text-sm">마감 전에 놓치지 마세요</p>
            </div>
            <Link href="/timedeals" className="text-sm text-[#4B6945] font-semibold hover:underline shrink-0">
              전체 보기 →
            </Link>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {timeDeals.map((deal) => (
              <TimeDealCard key={deal.id} deal={deal} />
            ))}
          </div>
        </section>
      )}

      <section className="mt-20">
        <h2 className="text-3xl font-bold mb-8">최근 등록된 캠핑장</h2>
        <div className="grid md:grid-cols-3 gap-8">
          {campings.map((camping) => (
            <Link key={camping.id} href={`/campings/${camping.id}`}>
              <div className="bg-white rounded-3xl shadow-lg p-4 hover:shadow-xl transition cursor-pointer">
                <div className="h-60 rounded-2xl mb-4 overflow-hidden">
                  <img
                    src={camping.firstImageUrl || "/images/default-camping.png"}
                    alt={camping.name}
                    className="w-full h-full object-cover"
                  />
                </div>
                <h3 className="font-bold text-xl">{camping.name}</h3>
                <p className="text-gray-500 mt-2">{camping.address}</p>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </>
  );
}