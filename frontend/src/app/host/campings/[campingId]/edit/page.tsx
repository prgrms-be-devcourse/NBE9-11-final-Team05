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
    return <p>수정 정보를 불러오는 중입니다...</p>;
  }

  return (
    <main>
      <HostCampingForm
        mode="edit"
        campingId={campingId}
        initialValues={initialValues}
      />
    </main>
  );
}