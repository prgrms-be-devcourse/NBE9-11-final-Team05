"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  CampingCreateRequest,
  CampingUpdateRequest,
  HostCampingFormMode,
  HostCampingFormValues,
  SiteFormValues,
} from "@/types/host";
import { createCamping, updateCamping } from "@/lib/api/host";

interface HostCampingFormProps {
  mode: HostCampingFormMode;
  campingId?: number;
  initialValues?: Partial<HostCampingFormValues>;
}

const defaultSite: SiteFormValues = {
  name: "",
  description: "",
  baseCapacity: "",
  maxCapacity: "",
  totalAmount: "",
  price: "",
};

const defaultValues: HostCampingFormValues = {
  tourNum: "",
  businessNum: "",
  firstImageUrl: "",
  name: "",
  homepage: "",
  region: "",
  city: "",
  address: "",
  description: "",
  phone: "",
  checkInTime: "",
  checkOutTime: "",
  notice: "",
  sites: [defaultSite],
};

export default function HostCampingForm({
  mode,
  campingId,
  initialValues,
}: HostCampingFormProps) {
  const router = useRouter();

  const [formValues, setFormValues] = useState<HostCampingFormValues>({
    ...defaultValues,
    ...initialValues,
    sites: initialValues?.sites ?? defaultValues.sites,
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  const isEditMode = mode === "edit";

  function handleChange(
    field: keyof Omit<HostCampingFormValues, "sites">,
    value: string
  ) {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
  }

  function handleSiteChange(
    index: number,
    field: keyof SiteFormValues,
    value: string
  ) {
    setFormValues((prev) => {
      const nextSites = [...prev.sites];
      nextSites[index] = {
        ...nextSites[index],
        [field]: value,
      };

      return {
        ...prev,
        sites: nextSites,
      };
    });
  }

  function addSite() {
    setFormValues((prev) => ({
      ...prev,
      sites: [...prev.sites, defaultSite],
    }));
  }

  function removeSite(index: number) {
    setFormValues((prev) => ({
      ...prev,
      sites: prev.sites.filter((_, siteIndex) => siteIndex !== index),
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);

    try {
      if (isEditMode) {
        if (!campingId) {
          alert("캠핑장 ID가 없습니다.");
          return;
        }

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
        return;
      }

      const request: CampingCreateRequest = {
        tourNum: formValues.tourNum || null,
        businessNum: formValues.businessNum || null,
        name: formValues.name,
        region: formValues.region,
        city: formValues.city,
        address: formValues.address,
        sites: formValues.sites.map((site) => ({
          name: site.name,
          description: site.description || null,
          baseCapacity: Number(site.baseCapacity),
          maxCapacity: Number(site.maxCapacity),
          totalAmount: Number(site.totalAmount),
          price: Number(site.price),
        })),
      };

      const createdCamping = await createCamping(request);
      router.push(`/host/campings/${createdCamping.id}`);
    } catch (error) {
      alert(
        isEditMode
          ? "캠핑장 수정에 실패했습니다."
          : "캠핑장 등록에 실패했습니다."
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2>{isEditMode ? "캠핑장 수정" : "캠핑장 등록"}</h2>

      <input
        placeholder="캠핑장명"
        value={formValues.name}
        onChange={(e) => handleChange("name", e.target.value)}
        required
      />

      <input
        placeholder="시/도"
        value={formValues.region}
        onChange={(e) => handleChange("region", e.target.value)}
        required
      />

      <input
        placeholder="시/군/구"
        value={formValues.city}
        onChange={(e) => handleChange("city", e.target.value)}
        required
      />

      <input
        placeholder="전체 주소"
        value={formValues.address}
        onChange={(e) => handleChange("address", e.target.value)}
        required
      />

      <input
        placeholder="전화번호"
        value={formValues.phone}
        onChange={(e) => handleChange("phone", e.target.value)}
      />

      <input
        placeholder="홈페이지"
        value={formValues.homepage}
        onChange={(e) => handleChange("homepage", e.target.value)}
      />

      <textarea
        placeholder="캠핑장 설명"
        value={formValues.description}
        onChange={(e) => handleChange("description", e.target.value)}
      />

      <textarea
        placeholder="공지사항"
        value={formValues.notice}
        onChange={(e) => handleChange("notice", e.target.value)}
      />

      <input
        type="time"
        value={formValues.checkInTime}
        onChange={(e) => handleChange("checkInTime", e.target.value)}
      />

      <input
        type="time"
        value={formValues.checkOutTime}
        onChange={(e) => handleChange("checkOutTime", e.target.value)}
      />

      {!isEditMode && (
        <section>
          <h3>구역 정보</h3>

          {formValues.sites.map((site, index) => (
            <div key={index}>
              <input
                placeholder="구역명"
                value={site.name}
                onChange={(e) =>
                  handleSiteChange(index, "name", e.target.value)
                }
                required
              />

              <input
                placeholder="기준 인원"
                value={site.baseCapacity}
                onChange={(e) =>
                  handleSiteChange(index, "baseCapacity", e.target.value)
                }
                required
              />

              <input
                placeholder="최대 인원"
                value={site.maxCapacity}
                onChange={(e) =>
                  handleSiteChange(index, "maxCapacity", e.target.value)
                }
                required
              />

              <input
                placeholder="구역 수"
                value={site.totalAmount}
                onChange={(e) =>
                  handleSiteChange(index, "totalAmount", e.target.value)
                }
                required
              />

              <input
                placeholder="가격"
                value={site.price}
                onChange={(e) =>
                  handleSiteChange(index, "price", e.target.value)
                }
                required
              />

              <textarea
                placeholder="구역 설명"
                value={site.description}
                onChange={(e) =>
                  handleSiteChange(index, "description", e.target.value)
                }
              />

              {formValues.sites.length > 1 && (
                <button type="button" onClick={() => removeSite(index)}>
                  구역 삭제
                </button>
              )}
            </div>
          ))}

          <button type="button" onClick={addSite}>
            구역 추가
          </button>
        </section>
      )}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting
          ? "처리 중..."
          : isEditMode
            ? "수정하기"
            : "등록하기"}
      </button>
    </form>
  );
}