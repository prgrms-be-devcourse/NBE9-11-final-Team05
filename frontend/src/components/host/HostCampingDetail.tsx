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

export default function HostCampingDetail({ camping }: HostCampingDetailProps) {
  return (
    <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      {camping.firstImageUrl && (
        <img
          src={camping.firstImageUrl}
          alt={camping.name}
          className="mb-6 h-64 w-full rounded-lg object-cover"
        />
      )}

      <div className="mb-6 flex items-center justify-between border-b border-gray-200 pb-4">
        <div>
          <h2 className="text-lg font-semibold text-gray-900">기본 정보</h2>
          <p className="mt-1 text-sm text-gray-500">
            호스트 캠핑장 상세 정보입니다.
          </p>
        </div>

        <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
          {getStatusLabel(camping.status)}
        </span>
      </div>

      <dl className="grid gap-4 text-sm md:grid-cols-2">
        <div>
          <dt className="text-gray-500">캠핑장명</dt>
          <dd className="mt-1 font-medium text-gray-900">{camping.name}</dd>
        </div>

        <div>
          <dt className="text-gray-500">주소</dt>
          <dd className="mt-1 font-medium text-gray-900">{camping.address}</dd>
        </div>

        <div>
          <dt className="text-gray-500">지역</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.region} {camping.city}
          </dd>
        </div>

        <div>
          <dt className="text-gray-500">평점</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.rating ?? "평점 없음"}
          </dd>
        </div>

        <div>
          <dt className="text-gray-500">관광사업자번호</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.tourNum ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-gray-500">사업자등록번호</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.businessNum ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-gray-500">전화번호</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.phone ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-gray-500">홈페이지</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.homepage ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-gray-500">체크인</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.checkInTime ?? "-"}
          </dd>
        </div>

        <div>
          <dt className="text-gray-500">체크아웃</dt>
          <dd className="mt-1 font-medium text-gray-900">
            {camping.checkOutTime ?? "-"}
          </dd>
        </div>
      </dl>

      <div className="mt-6 space-y-4 border-t border-gray-200 pt-6 text-sm">
        <div>
          <h3 className="font-semibold text-gray-900">캠핑장 설명</h3>
          <p className="mt-2 whitespace-pre-wrap text-gray-700">
            {camping.description ?? "설명이 없습니다."}
          </p>
        </div>

        <div>
          <h3 className="font-semibold text-gray-900">공지사항</h3>
          <p className="mt-2 whitespace-pre-wrap text-gray-700">
            {camping.notice ?? "-"}
          </p>
        </div>
      </div>
    </section>
  );
}