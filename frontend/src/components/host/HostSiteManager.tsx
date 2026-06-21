"use client";

import { FormEvent, useState } from "react";
import {
  createSite,
  deleteSite,
  updateSite,
} from "@/lib/api/host";
import { Site, SiteFormValues } from "@/types/host";

interface HostSiteManagerProps {
  campingId: number;
  sites: Site[];
}

const defaultSiteForm: SiteFormValues = {
  name: "",
  description: "",
  baseCapacity: "",
  maxCapacity: "",
  totalAmount: "",
  price: "",
};

const inputClass =
  "rounded-md border border-gray-300 px-3 py-2 text-sm";

export default function HostSiteManager({
  campingId,
  sites,
}: HostSiteManagerProps) {
  const [siteList, setSiteList] = useState<Site[]>(sites);
  const [formValues, setFormValues] =
    useState<SiteFormValues>(defaultSiteForm);

  const [editingSiteId, setEditingSiteId] = useState<number | null>(null);
  const [editValues, setEditValues] =
    useState<SiteFormValues>(defaultSiteForm);

  function handleChange(field: keyof SiteFormValues, value: string) {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
  }

  function handleEditChange(field: keyof SiteFormValues, value: string) {
    setEditValues((prev) => ({
      ...prev,
      [field]: value,
    }));
  }

  function startEdit(site: Site) {
    setEditingSiteId(site.id);
    setEditValues({
      name: site.name,
      description: site.description ?? "",
      baseCapacity: String(site.baseCapacity),
      maxCapacity: String(site.maxCapacity),
      totalAmount: String(site.totalAmount),
      price: String(site.price),
    });
  }

  function cancelEdit() {
    setEditingSiteId(null);
    setEditValues(defaultSiteForm);
  }

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    try {
      const createdSite = await createSite(campingId, {
        name: formValues.name,
        description: formValues.description || null,
        baseCapacity: Number(formValues.baseCapacity),
        maxCapacity: Number(formValues.maxCapacity),
        totalAmount: Number(formValues.totalAmount),
        price: Number(formValues.price),
      });

      setSiteList((prev) => [...prev, createdSite]);
      setFormValues(defaultSiteForm);
    } catch {
      alert("구역 등록에 실패했습니다.");
    }
  }

  async function handleUpdate(siteId: number) {
    try {
      const updatedSite = await updateSite(campingId, siteId, {
        name: editValues.name,
        description: editValues.description || null,
        baseCapacity: Number(editValues.baseCapacity),
        maxCapacity: Number(editValues.maxCapacity),
        totalAmount: Number(editValues.totalAmount),
        price: Number(editValues.price),
      });

      setSiteList((prev) =>
        prev.map((site) =>
          site.id === siteId ? updatedSite : site
        )
      );

      cancelEdit();
    } catch {
      alert("구역 수정에 실패했습니다.");
    }
  }

  async function handleDelete(siteId: number) {
    if (!confirm("구역을 삭제하시겠습니까?")) return;

    try {
      await deleteSite(campingId, siteId);
      setSiteList((prev) => prev.filter((site) => site.id !== siteId));
    } catch {
      alert("구역 삭제에 실패했습니다.");
    }
  }

  return (
    <section className="space-y-6 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <h2 className="text-lg font-semibold text-gray-900">구역 관리</h2>

      <div className="space-y-3">
        {siteList.length === 0 ? (
          <p className="text-sm text-gray-500">등록된 구역이 없습니다.</p>
        ) : (
          siteList.map((site) => (
            <article key={site.id} className="rounded-lg border border-gray-200 p-4">
              {editingSiteId === site.id ? (
                <div className="space-y-4">
                  <div className="grid gap-4 md:grid-cols-2">
                    <input className={inputClass} placeholder="구역명" value={editValues.name} onChange={(e) => handleEditChange("name", e.target.value)} required />
                    <input className={inputClass} placeholder="기준 인원" type="number" min={1} value={editValues.baseCapacity} onChange={(e) => handleEditChange("baseCapacity", e.target.value)} required />
                    <input className={inputClass} placeholder="최대 인원" type="number" min={1} value={editValues.maxCapacity} onChange={(e) => handleEditChange("maxCapacity", e.target.value)} required />
                    <input className={inputClass} placeholder="구역 수" type="number" min={1} value={editValues.totalAmount} onChange={(e) => handleEditChange("totalAmount", e.target.value)} required />
                    <input className={inputClass} placeholder="가격" type="number" min={1} value={editValues.price} onChange={(e) => handleEditChange("price", e.target.value)} required />
                  </div>

                  <textarea
                    placeholder="구역 설명"
                    value={editValues.description}
                    onChange={(e) => handleEditChange("description", e.target.value)}
                    className="min-h-20 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
                  />

                  <div className="flex justify-end gap-2">
                    <button type="button" onClick={cancelEdit} className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                      취소
                    </button>
                    <button type="button" onClick={() => handleUpdate(site.id)} className="rounded-md bg-gray-900 px-4 py-2 text-sm text-white hover:bg-gray-700">
                      저장
                    </button>
                  </div>
                </div>
              ) : (
                <div className="flex items-start justify-between gap-4">
                  <div className="space-y-1">
                    <h3 className="font-semibold text-gray-900">{site.name}</h3>
                    <p className="text-sm text-gray-600">{site.description ?? "설명 없음"}</p>
                    <p className="text-sm text-gray-600">기준 {site.baseCapacity}명 / 최대 {site.maxCapacity}명</p>
                    <p className="text-sm text-gray-600">수량: {site.totalAmount}</p>
                    <p className="text-sm text-gray-600">가격: {site.price.toLocaleString()}원</p>
                  </div>

                  <div className="flex gap-2">
                    <button type="button" onClick={() => startEdit(site)} className="text-sm text-gray-600 hover:text-gray-900">
                      수정
                    </button>
                    <button type="button" onClick={() => handleDelete(site.id)} className="text-sm text-red-500 hover:text-red-700">
                      삭제
                    </button>
                  </div>
                </div>
              )}
            </article>
          ))
        )}
      </div>

      <form onSubmit={handleCreate} className="space-y-4 border-t border-gray-200 pt-6">
        <h3 className="font-semibold text-gray-900">구역 추가</h3>

        <div className="grid gap-4 md:grid-cols-2">
          <input className={inputClass} placeholder="구역명" value={formValues.name} onChange={(e) => handleChange("name", e.target.value)} required />
          <input className={inputClass} placeholder="기준 인원" type="number" min={1} value={formValues.baseCapacity} onChange={(e) => handleChange("baseCapacity", e.target.value)} required />
          <input className={inputClass} placeholder="최대 인원" type="number" min={1} value={formValues.maxCapacity} onChange={(e) => handleChange("maxCapacity", e.target.value)} required />
          <input className={inputClass} placeholder="구역 수" type="number" min={1} value={formValues.totalAmount} onChange={(e) => handleChange("totalAmount", e.target.value)} required />
          <input className={inputClass} placeholder="가격" type="number" min={1} value={formValues.price} onChange={(e) => handleChange("price", e.target.value)} required />
        </div>

        <textarea
          placeholder="구역 설명"
          value={formValues.description}
          onChange={(e) => handleChange("description", e.target.value)}
          className="min-h-20 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        />

        <div className="flex justify-end">
          <button
            type="submit"
            className="rounded-md bg-gray-900 px-4 py-2 text-sm text-white hover:bg-gray-700"
          >
            구역 추가
          </button>
        </div>
      </form>
    </section>
  );
}