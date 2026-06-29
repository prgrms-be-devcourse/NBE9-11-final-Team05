"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  CampingCreateRequest,
  SiteFormValues,
} from "@/types/host";
import { createCamping } from "@/lib/api/host";
import { AppToast } from "@/lib/ui/toast";

const inputClass =
  "rounded-xl border border-gray-200 bg-white px-4 py-3 text-sm outline-none transition placeholder:text-gray-400 focus:border-[#D17A2F] focus:ring-2 focus:ring-[#D17A2F]/20";

const textareaClass =
  "min-h-24 w-full rounded-xl border border-gray-200 bg-white px-4 py-3 text-sm outline-none transition placeholder:text-gray-400 focus:border-[#D17A2F] focus:ring-2 focus:ring-[#D17A2F]/20";

const primaryButtonClass =
  "rounded-xl bg-[#D17A2F] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#BF6C26] disabled:opacity-50";

const secondaryButtonClass =
  "rounded-xl border border-gray-200 bg-white px-5 py-3 text-sm font-medium text-gray-700 transition hover:bg-gray-50";

const defaultSite: SiteFormValues = {
  name: "",
  description: "",
  baseCapacity: "",
  maxCapacity: "",
  totalAmount: "",
  price: "",
};

export default function HostCampingCreateForm() {
  const router = useRouter();

  const [formValues, setFormValues] = useState({
    tourNum: "",
    businessNum: "",
    name: "",
    region: "",
    city: "",
    address: "",
    sites: [defaultSite],
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(
    field: keyof Omit<typeof formValues, "sites">,
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
      sites: [...prev.sites, { ...defaultSite }],
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
    } catch {
      AppToast.error("캠핑장 등록에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-8 rounded-3xl border border-gray-100 bg-white p-8 shadow-sm"
    >
      <section className="space-y-4">
        <div>
          <h2 className="text-xl font-bold text-gray-900">기본 정보</h2>
          <p className="mt-1 text-sm text-gray-500">
            캠핑장 등록에 필요한 기본 정보를 입력해주세요.
          </p>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <input
            className={inputClass}
            placeholder="캠핑장명"
            value={formValues.name}
            onChange={(e) => handleChange("name", e.target.value)}
            required
          />
          <input
            className={inputClass}
            placeholder="관광사업자번호"
            value={formValues.tourNum}
            onChange={(e) => handleChange("tourNum", e.target.value)}
          />
          <input
            className={inputClass}
            placeholder="사업자등록번호"
            value={formValues.businessNum}
            onChange={(e) => handleChange("businessNum", e.target.value)}
          />
          <input
            className={inputClass}
            placeholder="시/도"
            value={formValues.region}
            onChange={(e) => handleChange("region", e.target.value)}
            required
          />
          <input
            className={inputClass}
            placeholder="시/군/구"
            value={formValues.city}
            onChange={(e) => handleChange("city", e.target.value)}
            required
          />
          <input
            className={inputClass}
            placeholder="전체 주소"
            value={formValues.address}
            onChange={(e) => handleChange("address", e.target.value)}
            required
          />
        </div>
      </section>

      <section className="space-y-4 border-t border-gray-100 pt-6">
        <div>
          <h3 className="text-xl font-bold text-gray-900">구역 정보</h3>
          <p className="mt-1 text-sm text-gray-500">
            예약 가능한 구역과 가격 정보를 등록해주세요.
          </p>
        </div>

        {formValues.sites.map((site, index) => (
          <div
            key={index}
            className="space-y-4 rounded-2xl border border-gray-100 bg-[#FAFAF7] p-5"
          >
            <div className="grid gap-4 md:grid-cols-2">
              <input
                className={inputClass}
                placeholder="구역명"
                value={site.name}
                onChange={(e) =>
                  handleSiteChange(index, "name", e.target.value)
                }
                required
              />
              <input
                className={inputClass}
                type="number"
                min={1}
                placeholder="기준 인원"
                value={site.baseCapacity}
                onChange={(e) =>
                  handleSiteChange(index, "baseCapacity", e.target.value)
                }
                required
              />
              <input
                className={inputClass}
                type="number"
                min={1}
                placeholder="최대 인원"
                value={site.maxCapacity}
                onChange={(e) =>
                  handleSiteChange(index, "maxCapacity", e.target.value)
                }
                required
              />
              <input
                className={inputClass}
                type="number"
                min={1}
                placeholder="구역 수"
                value={site.totalAmount}
                onChange={(e) =>
                  handleSiteChange(index, "totalAmount", e.target.value)
                }
                required
              />
              <input
                className={inputClass}
                type="number"
                min={1}
                placeholder="가격"
                value={site.price}
                onChange={(e) =>
                  handleSiteChange(index, "price", e.target.value)
                }
                required
              />
            </div>

            <textarea
              className={textareaClass}
              placeholder="구역 설명"
              value={site.description}
              onChange={(e) =>
                handleSiteChange(index, "description", e.target.value)
              }
            />

            {formValues.sites.length > 1 && (
              <button
                type="button"
                onClick={() => removeSite(index)}
                className="text-sm font-medium text-red-500 hover:text-red-700"
              >
                구역 삭제
              </button>
            )}
          </div>
        ))}

        <button
          type="button"
          onClick={addSite}
          className={secondaryButtonClass}
        >
          구역 추가 등록
        </button>
      </section>

      <div className="flex justify-end gap-2 border-t border-gray-100 pt-6">
        <button
          type="button"
          onClick={() => router.back()}
          className={secondaryButtonClass}
        >
          취소
        </button>

        <button
          type="submit"
          disabled={isSubmitting}
          className={primaryButtonClass}
        >
          {isSubmitting ? "처리 중..." : "등록하기"}
        </button>
      </div>
    </form>
  );
}