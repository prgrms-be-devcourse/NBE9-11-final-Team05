"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  CampingUpdateRequest,
  HostCampingFormValues,
} from "@/types/host";
import { updateCamping } from "@/lib/api/host";

interface HostCampingEditFormProps {
  campingId: number;
  initialValues: Partial<HostCampingFormValues>;
}

const inputClass =
  "rounded-md border border-gray-300 px-3 py-2 text-sm";

export default function HostCampingEditForm({
  campingId,
  initialValues,
}: HostCampingEditFormProps) {
  const router = useRouter();

  const [formValues, setFormValues] = useState({
    firstImageUrl: initialValues.firstImageUrl ?? "",
    name: initialValues.name ?? "",
    homepage: initialValues.homepage ?? "",
    region: initialValues.region ?? "",
    city: initialValues.city ?? "",
    address: initialValues.address ?? "",
    description: initialValues.description ?? "",
    phone: initialValues.phone ?? "",
    checkInTime: initialValues.checkInTime ?? "",
    checkOutTime: initialValues.checkOutTime ?? "",
    notice: initialValues.notice ?? "",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(field: keyof typeof formValues, value: string) {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);

    try {
      const request: CampingUpdateRequest = {
        firstImageUrl: formValues.firstImageUrl || null,
        name: formValues.name,
        homepage: formValues.homepage || null,
        region: formValues.region,
        city: formValues.city,
        address: formValues.address,
        description: formValues.description || null,
        phone: formValues.phone || null,
        checkInTime: formValues.checkInTime || null,
        checkOutTime: formValues.checkOutTime || null,
        notice: formValues.notice || null,
      };

      await updateCamping(campingId, request);
      router.push(`/host/campings/${campingId}`);
    } catch {
      alert("캠핑장 수정에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-8 rounded-lg border border-gray-200 bg-white p-6 shadow-sm"
    >
      <section className="space-y-4">
        <h2 className="text-lg font-semibold text-gray-900">기본 정보</h2>

        <div className="grid gap-4 md:grid-cols-2">
          <input className={inputClass} placeholder="캠핑장명" value={formValues.name} onChange={(e) => handleChange("name", e.target.value)} required />
          <input className={inputClass} placeholder="대표 이미지 URL" value={formValues.firstImageUrl} onChange={(e) => handleChange("firstImageUrl", e.target.value)} />
          <input className={inputClass} placeholder="시/도" value={formValues.region} onChange={(e) => handleChange("region", e.target.value)} required />
          <input className={inputClass} placeholder="시/군/구" value={formValues.city} onChange={(e) => handleChange("city", e.target.value)} required />
          <input className={inputClass} placeholder="전체 주소" value={formValues.address} onChange={(e) => handleChange("address", e.target.value)} required />
          <input className={inputClass} placeholder="전화번호" value={formValues.phone} onChange={(e) => handleChange("phone", e.target.value)} />
          <input className={inputClass} placeholder="홈페이지" value={formValues.homepage} onChange={(e) => handleChange("homepage", e.target.value)} />
        </div>

        <textarea className="min-h-28 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" placeholder="캠핑장 설명" value={formValues.description} onChange={(e) => handleChange("description", e.target.value)} />

        <textarea className="min-h-24 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" placeholder="공지사항" value={formValues.notice} onChange={(e) => handleChange("notice", e.target.value)} />

        <div className="grid gap-4 md:grid-cols-2">
          <input className={inputClass} type="time" value={formValues.checkInTime} onChange={(e) => handleChange("checkInTime", e.target.value)} />
          <input className={inputClass} type="time" value={formValues.checkOutTime} onChange={(e) => handleChange("checkOutTime", e.target.value)} />
        </div>
      </section>

      <div className="flex justify-end gap-2 border-t border-gray-200 pt-6">
        <button
          type="button"
          onClick={() => router.back()}
          className="rounded-md border border-gray-300 px-5 py-2 text-sm text-gray-700 hover:bg-gray-50"
        >
          취소
        </button>

        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-md bg-gray-900 px-5 py-2 text-sm text-white hover:bg-gray-700 disabled:opacity-50"
        >
          {isSubmitting ? "처리 중..." : "수정하기"}
        </button>
      </div>
    </form>
  );
}