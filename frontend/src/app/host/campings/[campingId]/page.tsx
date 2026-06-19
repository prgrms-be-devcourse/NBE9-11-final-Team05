"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { deleteCamping, getCampingDetail } from "@/lib/api/host";
import { CampingDetail } from "@/types/host";
import HostImageManager from "@/components/host/HostImageManager";
import HostSiteManager from "@/components/host/HostSiteManager";

export default function HostCampingDetailPage() {
  const params = useParams();
  const router = useRouter();
  const campingId = Number(params.campingId);

  const [camping, setCamping] = useState<CampingDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function fetchCamping() {
      try {
        const data = await getCampingDetail(campingId);
        setCamping(data);
      } catch {
        alert("캠핑장 정보를 불러오지 못했습니다.");
      } finally {
        setIsLoading(false);
      }
    }

    fetchCamping();
  }, [campingId]);

  async function handleDelete() {
    if (!confirm("캠핑장을 삭제하시겠습니까?")) return;

    try {
      await deleteCamping(campingId);
      alert("캠핑장이 삭제되었습니다.");
      router.push("/host/dashboard");
    } catch {
      alert("캠핑장 삭제에 실패했습니다.");
    }
  }

  if (isLoading) return <p>불러오는 중입니다...</p>;
  if (!camping) return <p>캠핑장 정보가 없습니다.</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{camping.name}</h1>
          <p className="mt-1 text-sm text-gray-500">{camping.address}</p>
        </div>
  
        <div className="flex gap-2">
          <Link
            href={`/host/campings/${campingId}/edit`}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            수정하기
          </Link>
  
          <button
            type="button"
            onClick={handleDelete}
            className="rounded-md bg-red-500 px-4 py-2 text-sm text-white hover:bg-red-600"
          >
            삭제하기
          </button>
        </div>
      </div>
  
      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        {camping.firstImageUrl && (
          <img
            src={camping.firstImageUrl}
            alt={camping.name}
            className="mb-6 h-64 w-full rounded-lg object-cover"
          />
        )}
  
        <div className="space-y-3 text-sm text-gray-700">
          <p>{camping.description ?? "설명이 없습니다."}</p>
          <p>전화번호: {camping.phone ?? "-"}</p>
          <p>홈페이지: {camping.homepage ?? "-"}</p>
          <p>
            체크인 {camping.checkInTime ?? "-"} / 체크아웃{" "}
            {camping.checkOutTime ?? "-"}
          </p>
          <p>공지사항: {camping.notice ?? "-"}</p>
          <p>평점: {camping.rating ?? "평점 없음"}</p>
        </div>
      </section>
  
      <HostImageManager campingId={campingId} imageUrls={camping.imageUrls} />
  
      <HostSiteManager campingId={campingId} sites={camping.sites} />
    </div>
  );
}