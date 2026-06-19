"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import HostCampingCard from "@/components/host/HostCampingCard";
import { getMyCampings } from "@/lib/api/host";
import { HostCampingListItem } from "@/types/host";

export default function HostCampingList() {
  const [campings, setCampings] = useState<HostCampingListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    async function fetchCampings() {
      try {
        const data = await getMyCampings();
        setCampings(data);
      } catch (error) {
        setErrorMessage("캠핑장 목록을 불러오지 못했습니다.");
      } finally {
        setIsLoading(false);
      }
    }

    fetchCampings();
  }, []);

  if (isLoading) {
    return <p>캠핑장 목록을 불러오는 중입니다...</p>;
  }

  if (errorMessage) {
    return <p>{errorMessage}</p>;
  }

  if (campings.length === 0) {
    return (
      <div className="rounded-lg border border-dashed border-gray-300 bg-white p-10 text-center">
  <p className="mb-4 text-gray-600">
    등록된 캠핑장이 없습니다.
  </p>

  <Link
    href="/host/campings/new"
    className="inline-flex rounded-md bg-gray-900 px-4 py-2 text-sm text-white hover:bg-gray-700"
  >
    캠핑장 등록하기
  </Link>
</div>
    );
  }

  return (
    <section className="space-y-6">
  <div className="flex items-center justify-between">
    <h2 className="text-2xl font-bold text-gray-900">
      내 캠핑장 목록
    </h2>

    <Link
      href="/host/campings/new"
      className="rounded-md bg-gray-900 px-4 py-2 text-sm text-white hover:bg-gray-700"
    >
      캠핑장 등록
    </Link>
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