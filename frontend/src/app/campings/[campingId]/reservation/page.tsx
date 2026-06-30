"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import ReservationForm from "@/components/reservation/ReservationForm";
import { getCampingDetailClient } from "@/lib/api/reservation.client";
import type { CampingDetail } from "@/types/camping";

export default function ReservationPage() {
  const params = useParams();
  const campingId = String(params.campingId);

  const [camping, setCamping] = useState<CampingDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  // 🔥 sessionStorage state
  const [draft, setDraft] = useState<any>(null);

  useEffect(() => {
    const data = sessionStorage.getItem("reservationDraft");

    if (data) {
      setDraft(JSON.parse(data));
    }
  }, []);

  useEffect(() => {
    getCampingDetailClient(Number(campingId))
      .then(setCamping)
      .catch(() =>
        setError("캠핑장 정보를 불러오지 못했습니다.")
      );
  }, [campingId]);

  if (error)
    return <p className="p-10 text-center text-red-500">{error}</p>;

  if (!camping || !draft)
    return (
      <div className="p-10 text-center">
        <p className="text-stone-400 mb-4">불러오는 중...</p>
        <p className="text-xs text-stone-500">
          오랫동안 반응이 없다면{" "}
          <a href={`/campings/${campingId}`} className="text-orange-500 underline">
            상세 페이지
          </a>
          에서 다시 시도해주세요.
        </p>
      </div>
    );

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
        defaultCheckIn={draft.checkIn}
        defaultCheckOut={draft.checkOut}
        defaultSiteId={draft.site.id}
      />
    </div>
  );
}