"use client";

import { useEffect, useState } from "react";
import { useParams, useSearchParams } from "next/navigation";
import ReservationForm from "@/components/reservation/ReservationForm";
import { getCampingDetailClient } from "@/lib/api/reservation.client";
import type { CampingDetail } from "@/types/camping";

export default function ReservationPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const campingId = String(params.campingId);

  const checkIn = searchParams.get("checkIn") ?? "";
  const checkOut = searchParams.get("checkOut") ?? "";

  const [camping, setCamping] = useState<CampingDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getCampingDetailClient(Number(campingId))
      .then(setCamping)
      .catch(() => setError("캠핑장 정보를 불러오지 못했습니다."));
  }, [campingId]);

  if (error) return <p className="p-10 text-center text-red-500">{error}</p>;
  if (!camping) return <p className="p-10 text-center text-stone-400">불러오는 중...</p>;

  const siteOptions = camping.sites.map((site) => ({
    id: site.id,
    name: `${site.name} (최대 ${site.maxCapacity}명 / ${site.price.toLocaleString()}원)`,
    price: site.price,
    baseCapacity: site.baseCapacity,
    maxCapacity: site.maxCapacity,
  }));

  return (
    <div className="p-6">
      <ReservationForm
        campingId={campingId}
        siteOptions={siteOptions}
        campingName={camping.name}
        defaultCheckIn={checkIn}
        defaultCheckOut={checkOut}
      />
    </div>
  );
}