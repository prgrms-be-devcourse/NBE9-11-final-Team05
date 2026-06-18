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

export default function HostSiteManager({
  campingId,
  sites,
}: HostSiteManagerProps) {
  const [siteList, setSiteList] = useState<Site[]>(sites);
  const [formValues, setFormValues] =
    useState<SiteFormValues>(defaultSiteForm);

  function handleChange(field: keyof SiteFormValues, value: string) {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
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

  async function handleDelete(siteId: number) {
    if (!confirm("구역을 삭제하시겠습니까?")) return;

    try {
      await deleteSite(campingId, siteId);
      setSiteList((prev) => prev.filter((site) => site.id !== siteId));
    } catch {
      alert("구역 삭제에 실패했습니다.");
    }
  }

  async function handleUpdate(site: Site) {
    const name = prompt("구역명", site.name);
    if (!name) return;

    try {
      const updatedSite = await updateSite(campingId, site.id, {
        name,
      });

      setSiteList((prev) =>
        prev.map((item) =>
          item.id === site.id ? updatedSite : item
        )
      );
    } catch {
      alert("구역 수정에 실패했습니다.");
    }
  }

  return (
    <section>
      <h2>구역 관리</h2>

      <form onSubmit={handleCreate}>
        <input
          placeholder="구역명"
          value={formValues.name}
          onChange={(e) => handleChange("name", e.target.value)}
          required
        />

        <input
          placeholder="기준 인원"
          type="number"
          min={1}
          value={formValues.baseCapacity}
          onChange={(e) => handleChange("baseCapacity", e.target.value)}
          required
        />

        <input
          placeholder="최대 인원"
          type="number"
          min={1}
          value={formValues.maxCapacity}
          onChange={(e) => handleChange("maxCapacity", e.target.value)}
          required
        />

        <input
          placeholder="구역 수"
          type="number"
          min={1}
          value={formValues.totalAmount}
          onChange={(e) => handleChange("totalAmount", e.target.value)}
          required
        />

        <input
          placeholder="가격"
          type="number"
          min={1}
          value={formValues.price}
          onChange={(e) => handleChange("price", e.target.value)}
          required
        />

        <textarea
          placeholder="구역 설명"
          value={formValues.description}
          onChange={(e) => handleChange("description", e.target.value)}
        />

        <button type="submit">구역 추가</button>
      </form>

      <div>
        {siteList.length === 0 ? (
          <p>등록된 구역이 없습니다.</p>
        ) : (
          siteList.map((site) => (
            <article key={site.id}>
              <h3>{site.name}</h3>
              <p>{site.description ?? "설명 없음"}</p>
              <p>
                기준 {site.baseCapacity}명 / 최대 {site.maxCapacity}명
              </p>
              <p>수량: {site.totalAmount}</p>
              <p>가격: {site.price.toLocaleString()}원</p>

              <button type="button" onClick={() => handleUpdate(site)}>
                수정
              </button>

              <button type="button" onClick={() => handleDelete(site.id)}>
                삭제
              </button>
            </article>
          ))
        )}
      </div>
    </section>
  );
}