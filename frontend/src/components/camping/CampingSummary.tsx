import { CampingDetail } from "@/types/camping";

interface Props {
  camping: CampingDetail;
}

export default function CampingSummary({ camping }: Props) {
  return (
    <section className="bg-[#F6F8F4]/80 rounded-3xl shadow-md p-8 border border-white/60">

      <h1 className="text-2xl font-bold text-gray-900 leading-tight">
        {camping.name}
      </h1>

      {camping.rating != null && (
        <div className="mt-3 flex items-center gap-2 text-sm text-gray-600">
          <span className="text-yellow-500">⭐</span>
          <span>{camping.rating.toFixed(1)}</span>
        </div>
      )}

      <div className="mt-2 text-sm text-gray-500">
        📍 {camping.address}
      </div>

      {camping.homepage && (
        <div className="mt-2 text-sm text-blue-600">
          🌐{" "}
          <a
            href={camping.homepage}
            target="_blank"
            rel="noopener noreferrer"
            className="hover:underline"
          >
            홈페이지 바로가기
          </a>
        </div>
      )}

      <div className="mt-5 grid grid-cols-2 gap-3 text-xs text-gray-600">

        {/* 🕒 체크인 */}
        {camping.checkInTime && (
          <div className="flex items-center gap-2">
            <span>🕒</span>
            <span>CHECK-IN: {camping.checkInTime}</span>
          </div>
        )}

        {/* 🕚 체크아웃 */}
        {camping.checkOutTime && (
          <div className="flex items-center gap-2">
            <span>🕚</span>
            <span>CHECK-OUT: {camping.checkOutTime}</span>
          </div>
        )}

      </div>

    </section>
  );
}