"use client";

import { useEffect, useState } from "react";
import { AdminDashboardResponse } from "@/types/admin";
import { getDashboard } from "@/lib/api/admin";
import Link from "next/link";

export default function DashboardPage() {
  const [dashboard, setDashboard] =
    useState<AdminDashboardResponse | null>(null);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const data = await getDashboard();
        setDashboard(data);
      } catch (error) {
        console.error(error);
      }
    };

    loadDashboard();
  }, []);

  if (!dashboard) {
    return (
      <div className="flex items-center justify-center min-h-[400px] text-gray-400">
        데이터를 불러오는 중...
      </div>
    );
  }

  return (
    <div className="bg-[#F5F6F2] min-h-screen p-8">
      {/* 제목 */}
      <div className="mb-10">
        <h1 className="text-4xl font-bold text-[#2F3A2F]">
          관리자 대시보드
        </h1>

        <p className="mt-2 text-gray-500">
          캠핑가잣 서비스 현황을 확인할 수 있습니다.
        </p>
      </div>

      {/* 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

        {/* 총 수익 */}
        <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 hover:shadow-md transition">
          <div className="w-12 h-1 rounded-full bg-[#5C7A5C]" />

          <p className="mt-6 text-sm text-gray-400">
            총 수익
          </p>

          <h2 className="mt-3 text-4xl font-bold text-[#5C7A5C]">
            {dashboard.totalSalesAmount.toLocaleString()}원
          </h2>

          <p className="mt-4 text-sm text-[#F4A261]">
            누적 매출
          </p>
        </div>

        {/* 활동 회원 */}
        <Link
          href="/admin/members"
          className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 hover:shadow-md transition block"
        >
          <div className="w-12 h-1 rounded-full bg-[#5C7A5C]" />

          <p className="mt-6 text-sm text-gray-400">
            활동 회원 수
          </p>

          <h2 className="mt-3 text-4xl font-bold text-[#5C7A5C]">
            {dashboard.activeUserCount.toLocaleString()}명
          </h2>

          <p className="mt-4 text-sm text-[#F4A261]">
            현재 활성 사용자
          </p>
        </Link>

        {/* 승인 대기 */}
        <Link
          href="/admin/camping-approvals"
          className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 hover:shadow-md transition block"
        >
          <div className="w-12 h-1 rounded-full bg-[#5C7A5C]" />

          <p className="mt-6 text-sm text-gray-400">
            승인 대기 캠핑장
          </p>

          <h2 className="mt-3 text-4xl font-bold text-[#5C7A5C]">
            {dashboard.pendingCampingCount.toLocaleString()}개
          </h2>

          <p className="mt-4 text-sm text-[#F4A261]">
            검토 필요
          </p>
        </Link>

      </div>
    </div>
  );
}