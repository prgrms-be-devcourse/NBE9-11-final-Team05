import { HostCampingDetail as HostCampingDetailType } from "@/types/host";

interface HostCampingDetailProps {
  camping: HostCampingDetailType;
}

function getStatusLabel(status: HostCampingDetailType["status"]) {
  switch (status) {
    case "PENDING":
      return "승인 대기";
    case "APPROVED":
      return "승인 완료";
    case "REJECTED":
      return "승인 거절";
    default:
      return status;
  }
}

function formatHour(time?: string | null) {
  if (!time) return "-";

  return `${Number(time.slice(0, 2))}시`;
}

export default function HostCampingDetail({ camping }: HostCampingDetailProps) {
  return (
    <section className="rounded-3xl border border-gray-100 bg-white p-8 shadow-sm">
      {camping.firstImageUrl && (
        <div className="mb-8 overflow-hidden rounded-2xl bg-gray-50">
          <img
            src={camping.firstImageUrl}
            alt={camping.name}
            className="max-h-[520px] w-full object-contain"
          />
        </div>
      )}

      <div className="mb-6 flex items-center justify-between border-b border-gray-100 pb-5">
        <div>
          <h2 className="text-xl font-bold text-gray-900">기본 정보</h2>
          <p className="mt-1 text-sm text-gray-500">
            호스트 캠핑장 상세 정보입니다.
          </p>
        </div>

        <span className="rounded-full bg-[#F4F5F1] px-4 py-2 text-xs font-semibold text-[#3F6B3F]">
          {getStatusLabel(camping.status)}
        </span>
      </div>

      <dl className="grid gap-x-16 gap-y-6 text-sm md:grid-cols-2">
        <div>
          <dt className="text-xs font-semibold text-gray-400">캠핑장명</dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {camping.name}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">주소</dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {camping.address}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">지역</dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {camping.region} {camping.city}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">평점</dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {camping.rating ?? "평점 없음"}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">
            관광사업자번호
          </dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {camping.tourNum ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">
            사업자등록번호
          </dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {camping.businessNum ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">전화번호</dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {camping.phone ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">홈페이지</dt>
          <dd className="mt-1.5 break-words text-base font-semibold text-gray-900">
            {camping.homepage ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">체크인</dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {formatHour(camping.checkInTime)}
          </dd>
        </div>

        <div>
          <dt className="text-xs font-semibold text-gray-400">체크아웃</dt>
          <dd className="mt-1.5 text-base font-semibold text-gray-900">
            {formatHour(camping.checkOutTime)}
          </dd>
        </div>
      </dl>

      <div className="mt-8 space-y-6 border-t border-gray-100 pt-8 text-sm">
        <div>
          <h3 className="font-semibold text-gray-900">캠핑장 설명</h3>
          <p className="mt-2 whitespace-pre-wrap leading-7 text-gray-700">
            {camping.description ?? "설명이 없습니다."}
          </p>
        </div>

        <div>
          <h3 className="font-semibold text-gray-900">공지사항</h3>
          <p className="mt-2 whitespace-pre-wrap leading-7 text-gray-700">
            {camping.notice ?? "-"}
          </p>
        </div>
      </div>
    </section>
  );
}