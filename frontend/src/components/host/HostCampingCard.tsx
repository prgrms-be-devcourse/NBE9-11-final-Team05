import Link from "next/link";
import { HostCampingListItem } from "@/types/host";

interface HostCampingCardProps {
  camping: HostCampingListItem;
}

export default function HostCampingCard({ camping }: HostCampingCardProps) {
  return (
    <Link href={`/host/campings/${camping.id}`}>
      <article className="flex gap-4 rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition hover:shadow-md">
        <div className="h-24 w-32 shrink-0 overflow-hidden rounded-md bg-gray-100">
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
  
            <span className="rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-600">
              {camping.rating !== null
                ? `★ ${camping.rating.toFixed(1)}`
                : "평점 없음"}
            </span>
          </div>
  
          <p className="text-sm text-gray-600">
            {camping.region} {camping.city}
          </p>
        </div>
      </article>
    </Link>
  );
}