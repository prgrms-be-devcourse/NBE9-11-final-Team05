"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import HostCampingCard from "@/components/host/HostCampingCard";
import { getMyCampings } from "@/lib/api/host";
import { HostCampingListItem } from "@/types/host";

const primaryButtonClass =
  "rounded-xl bg-[#D17A2F] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#BF6C26]";

export default function HostCampingList() {
  const [campings, setCampings] = useState<HostCampingListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    async function fetchCampings() {
      try {
        const data = await getMyCampings();
        setCampings(data);
      } catch {
        setErrorMessage("캠핑장 목록을 불러오지 못했습니다.");
      } finally {
        setIsLoading(false);
      }
    }

    fetchCampings();
  }, []);

  if (isLoading) {
    return (
      <p className="text-sm text-gray-500">
        캠핑장 목록을 불러오는 중입니다...
      </p>
    );
  }

  if (errorMessage) {
    return <p className="text-sm text-red-500">{errorMessage}</p>;
  }

  if (campings.length === 0) {
    return (
      <div className="rounded-3xl border border-dashed border-gray-200 bg-white p-12 text-center shadow-sm">
        <p className="mb-2 text-lg font-semibold text-gray-800">
          등록된 캠핑장이 없습니다.
        </p>

        <p className="mb-6 text-sm text-gray-500">
          새 캠핑장을 등록하거나 기존 캠핑장을 인증하여 관리할 수 있습니다.
        </p>

        <div className="flex justify-center gap-3">
          <Link
            href="/host/campings/new"
            className={`inline-flex ${primaryButtonClass}`}
          >
            캠핑장 등록하기
          </Link>

          <Link
            href="/host/campings/claim"
            className="inline-flex rounded-xl border border-[#D17A2F] px-5 py-3 text-sm font-semibold text-[#D17A2F] transition hover:bg-orange-50"
          >
            내 캠핑장 찾기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <section className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">
            내 캠핑장 목록
          </h2>
          <p className="mt-1 text-sm text-gray-500">
            등록한 캠핑장을 확인하고 관리할 수 있습니다.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            href="/host/campings/claim"
            className="rounded-xl border border-[#D17A2F] px-5 py-3 text-sm font-semibold text-[#D17A2F] hover:bg-orange-50"
          >
            내 캠핑장 찾기
          </Link>

          <Link
            href="/host/campings/new"
            className={primaryButtonClass}
          >
            캠핑장 등록
          </Link>
        </div>
      </div>

      <div className="flex flex-col gap-4">
        {campings.map((camping) => (
          <HostCampingCard
            key={camping.id}
            camping={camping}
          />
        ))}
      </div>
    </section>
  );
}