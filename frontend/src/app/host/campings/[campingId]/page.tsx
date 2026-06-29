"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  deleteCamping,
  getHostCampingDetail,
  getHostSites,
} from "@/lib/api/host";
import {
  HostCampingDetail as HostCampingDetailType,
  Site,
} from "@/types/host";
import HostCampingDetail from "@/components/host/HostCampingDetail";
import HostSiteManager from "@/components/host/HostSiteManager";
import HostImageManager from "@/components/host/HostImageManager";
import { Pencil, Trash2 } from "lucide-react";

export default function HostCampingDetailPage() {
  const params = useParams();
  const router = useRouter();
  const campingId = Number(params.campingId);

  const [camping, setCamping] = useState<HostCampingDetailType | null>(null);
  const [sites, setSites] = useState<Site[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  async function fetchCamping() {
    try {
      const [campingData, siteData] = await Promise.all([
        getHostCampingDetail(campingId),
        getHostSites(campingId),
      ]);

      setCamping(campingData);
      setSites(siteData);
    } catch {
      alert("캠핑장 정보를 불러오지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
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

        <div className="flex items-center gap-3">
          <Link
            href={`/host/campings/${campingId}/edit`}
            className="inline-flex items-center gap-2 rounded-xl border border-gray-300 bg-white px-5 py-3 text-sm font-medium text-gray-700 transition hover:border-gray-400 hover:bg-gray-50"
          >
            <Pencil className="h-4 w-4" />
            수정하기
          </Link>

          <button
            type="button"
            onClick={handleDelete}
            className="inline-flex items-center gap-2 rounded-xl border border-red-300 bg-white px-5 py-3 text-sm font-medium text-red-600 transition hover:bg-red-50"
          >
            <Trash2 className="h-4 w-4" />
            삭제하기
          </button>
        </div>
      </div>

      <HostCampingDetail camping={camping} />

      <HostImageManager
        campingId={campingId}
        initialImages={camping.images}
        onChange={fetchCamping}
      />

      <HostSiteManager campingId={campingId} sites={sites} />
    </div>
  );
}