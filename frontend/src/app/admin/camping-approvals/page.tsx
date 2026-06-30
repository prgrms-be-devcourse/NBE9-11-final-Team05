"use client";

import { useEffect, useState } from "react";
import {
  approveCamping,
  approveCampingBulk,
  getPendingCampings,
  rejectCamping,
} from "@/lib/api/admin";
import { PendingCampingResponse } from "@/types/admin";
import { AppToast } from "@/lib/ui/toast";

export default function PendingCampingPage() {
  const [campings, setCampings] = useState<PendingCampingResponse[]>([]);
  const [loadingAll, setLoadingAll] = useState(false);

  const isEmpty = campings.length === 0;

  useEffect(() => {
    const loadCampings = async () => {
      try {
        const data = await getPendingCampings();
        setCampings(data.content);
      } catch (error) {
        console.error(error);
      }
    };

    loadCampings();
  }, []);

  // 단건 승인
  const handleApprove = async (campingId: number) => {
    try {
      await approveCamping(campingId);

      AppToast.success("승인되었습니다.");
      
      const data = await getPendingCampings();
      setCampings(data.content);
    } catch (error) {
      console.error(error);
    }
  };

  // 단건 반려
  const handleReject = async (campingId: number) => {
    const reason = prompt("반려 사유를 입력해주세요.");

    if (!reason) return;

    try {
      await rejectCamping(campingId, reason);

      AppToast.success("반려되었습니다.");

      const data = await getPendingCampings();
      setCampings(data.content);
    } catch (error) {
      console.error(error);
    }
  };

  // 일괄 승인
  const handleApproveAll = async () => {
    if (isEmpty) {
      AppToast.error("승인할 캠핑장이 없습니다.");
      return;
    }

    const confirmAll = confirm("모든 캠핑장을 일괄 승인하시겠습니까?");
    if (!confirmAll) return;

    try {
      setLoadingAll(true);

      const ids = campings.map((c) => c.campingId);

      await approveCampingBulk(ids);

      AppToast.success("일괄 승인 완료");

      const data = await getPendingCampings();
      setCampings(data.content);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingAll(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F5F6F2] p-8">
      {/* 헤더 */}
      <div className="mb-8 flex items-end justify-between">
        <div>
          <h1 className="text-3xl font-bold text-[#2F3A2F]">
            승인 대기 캠핑장
          </h1>
          <p className="mt-2 text-gray-500">
            관리자 승인을 기다리는 캠핑장 목록입니다.
          </p>
        </div>

        {/* 일괄 승인 버튼 */}
        <button
          onClick={handleApproveAll}
          disabled={isEmpty || loadingAll}
          className={`
            px-5 py-3 rounded-xl font-semibold transition
            ${
              isEmpty || loadingAll
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                : "bg-[#2F3A2F] text-white hover:bg-[#1f271f]"
            }
          `}
        >
          {loadingAll
            ? "처리 중..."
            : isEmpty
            ? "승인할 캠핑장 없음"
            : "일괄 승인"}
        </button>
      </div>

      {/* 테이블 */}
      <div className="overflow-hidden rounded-3xl bg-white shadow-sm border border-gray-100">
        <table className="w-full">
          <thead className="bg-[#5C7A5C] text-white">
            <tr>
              <th className="px-6 py-4 text-left">캠핑장명</th>
              <th className="px-6 py-4 text-left">호스트</th>
              <th className="px-6 py-4 text-left">사업자 번호</th>
              <th className="px-6 py-4 text-left">등록일</th>
              <th className="px-6 py-4 text-center">관리</th>
            </tr>
          </thead>

          <tbody>
            {/* EMPTY STATE */}
            {isEmpty ? (
              <tr>
                <td
                  colSpan={5}
                  className="py-10 text-center text-gray-400"
                >
                  승인 대기 중인 캠핑장이 없습니다.
                </td>
              </tr>
            ) : (
              campings.map((camping) => (
                <tr
                  key={camping.campingId}
                  className="border-b border-gray-100 hover:bg-gray-50"
                >
                  <td className="px-6 py-5 font-medium text-gray-800">
                    {camping.campingName}
                  </td>

                  <td className="px-6 py-5 text-gray-500">
                    {camping.hostName}
                  </td>

                  <td className="px-6 py-5 text-gray-500">
                    {camping.businessNum}
                  </td>

                  <td className="px-6 py-5 text-gray-500">
                    {camping.createdAt}
                  </td>

                  <td className="px-6 py-5">
                    <div className="flex justify-center gap-2">
                      <button
                        onClick={() =>
                          handleApprove(camping.campingId)
                        }
                        className="px-4 py-2 rounded-xl bg-[#5C7A5C] text-white hover:bg-[#4F694F]"
                      >
                        승인
                      </button>

                      <button
                        onClick={() =>
                          handleReject(camping.campingId)
                        }
                        className="px-4 py-2 rounded-xl bg-[#D13B61] text-white hover:bg-[#B0284B]"
                      >
                        반려
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}