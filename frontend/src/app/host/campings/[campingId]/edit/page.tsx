"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import HostCampingForm from "@/components/host/HostCampingForm";
import { getCampingDetail } from "@/lib/api/host";
import { CampingDetail, HostCampingFormValues } from "@/types/host";

export default function HostCampingEditPage() {
  const params = useParams();
  const campingId = Number(params.campingId);

  const [initialValues, setInitialValues] =
    useState<Partial<HostCampingFormValues> | null>(null);

  useEffect(() => {
    async function fetchCamping() {
      const camping: CampingDetail = await getCampingDetail(campingId);

      setInitialValues({
        firstImageUrl: camping.firstImageUrl ?? "",
        name: camping.name,
        homepage: camping.homepage ?? "",
        // region: camping.region,
        // city: camping.city,
        address: camping.address,
        description: camping.description ?? "",
        phone: camping.phone ?? "",
        checkInTime: camping.checkInTime ?? "",
        checkOutTime: camping.checkOutTime ?? "",
        notice: camping.notice ?? "",
      });
    }

    fetchCamping();
  }, [campingId]);

  if (!initialValues) {
    return <p className="text-sm text-gray-500">수정 정보를 불러오는 중입니다...</p>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">캠핑장 수정</h1>
        <p className="mt-1 text-sm text-gray-500">
          캠핑장 기본 정보를 수정할 수 있습니다.
        </p>
      </div>

      <HostCampingForm
        mode="edit"
        campingId={campingId}
        initialValues={initialValues}
      />
    </div>
  );
}