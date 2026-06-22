import Link from "next/link";
import { HostCampingListItem } from "@/types/host";

interface HostCampingCardProps {
  camping: HostCampingListItem;
}

function getStatusLabel(status: HostCampingListItem["status"]) {
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

export default function HostCampingCard({ camping }: HostCampingCardProps) {
  return (
    <Link href={`/host/campings/${camping.id}`}>
      <article className="flex gap-5 rounded-2xl border border-gray-100 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg">
        <div className="h-28 w-36 shrink-0 overflow-hidden rounded-2xl bg-[#F4F5F1]">
          {camping.firstImageUrl ? (
            <img
              src={camping.firstImageUrl}
              alt={camping.name}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-xs text-gray-400">
              이미지 없음
            </div>
          )}
        </div>

        <div className="flex flex-1 flex-col justify-center">
          <div className="mb-2 flex items-center justify-between gap-2">
            <h3 className="text-lg font-semibold text-gray-900">
              {camping.name}
            </h3>

            <div className="flex items-center gap-2">
              <span className="rounded-full bg-[#F4F5F1] px-3 py-1 text-xs font-medium text-[#3F6B3F]">
                {getStatusLabel(camping.status)}
              </span>

              <span className="rounded-full bg-[#F4F5F1] px-3 py-1 text-xs font-medium text-[#3F6B3F]">
                {camping.rating !== null
                  ? `★ ${camping.rating.toFixed(1)}`
                  : "평점 없음"}
              </span>
            </div>
          </div>

          <p className="text-sm text-gray-600">
            {camping.region} {camping.city}
          </p>
        </div>
      </article>
    </Link>
  );
}