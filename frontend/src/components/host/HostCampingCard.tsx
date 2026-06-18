import Link from "next/link";
import { HostCampingListItem } from "@/types/host";

interface HostCampingCardProps {
  camping: HostCampingListItem;
}

export default function HostCampingCard({ camping }: HostCampingCardProps) {
  return (
    <Link href={`/host/campings/${camping.id}`}>
      <article className="rounded-lg border border-gray-200 bg-white shadow-sm transition hover:shadow-md">
        <div className="h-40 w-full overflow-hidden rounded-t-lg bg-gray-100">
          {camping.firstImageUrl ? (
            <img
              src={camping.firstImageUrl}
              alt={camping.name}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-sm text-gray-400">
              등록된 이미지가 없습니다
            </div>
          )}
        </div>

        <div className="p-4">
          <div className="mb-2 flex items-start justify-between gap-2">
            <h3 className="line-clamp-1 text-lg font-semibold text-gray-900">
              {camping.name}
            </h3>

            <span className="shrink-0 rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-600">
              {camping.rating !== null ? `★ ${camping.rating.toFixed(1)}` : "평점 없음"}
            </span>
          </div>

          <p className="mb-1 text-sm text-gray-600">
            {camping.region} {camping.city}
          </p>

        </div>
      </article>
    </Link>
  );
}