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
      <div>
        <p>등록된 캠핑장이 없습니다.</p>

        <Link href="/host/campings/new">캠핑장 등록하기</Link>
      </div>
    );
  }

  return (
    <section>
      <div>
        <h2>내 캠핑장 목록</h2>

        <Link href="/host/campings/new">캠핑장 등록하기</Link>
      </div>

      <div>
        {campings.map((camping) => (
          <HostCampingCard key={camping.id} camping={camping} />
        ))}
      </div>
    </section>
  );
}