"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { CampingDetail } from "@/types/camping";
import { UserRole } from "@/lib/utils/auth";
import { AppToast } from "@/lib/ui/toast";
import { getAvailableSites } from "@/lib/api/camping";

interface Props {
  camping: CampingDetail;
  role: UserRole | null;
}

export default function ReservationCard({ camping, role }: Props) {
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [availableSites, setAvailableSites] = useState<any[]>([]);
  const [selectedSite, setSelectedSite] = useState<any | null>(null);
  const [loading, setLoading] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const router = useRouter();
  const today = new Date().toLocaleDateString("sv-SE");

  // 박 수 계산
  const getNights = (inDate: string, outDate: string) => {
    if (!inDate || !outDate) return 0;

    const start = new Date(inDate);
    const end = new Date(outDate);

    const diff = end.getTime() - start.getTime();
    return diff / (1000 * 60 * 60 * 24);
  };

  const nights = getNights(checkIn, checkOut);

  const totalPrice = selectedSite ? selectedSite.price * nights : 0;

  const getMinCheckOut = (checkIn: string) => {
    if (!checkIn) return "";
    const date = new Date(checkIn);
    date.setDate(date.getDate() + 1);
    return date.toISOString().split("T")[0];
  };

  const fetchAvailableSites = async (inDate: string, outDate: string) => {
    if (!inDate || !outDate) return;

    try {
      setLoading(true);
      const data = await getAvailableSites(camping.id, inDate, outDate);
      setAvailableSites(data);
      setSelectedSite(null);
    } catch {
      AppToast.error("예약 가능한 구역 조회 실패");
    } finally {
      setLoading(false);
    }
  };

  const handleFinalReservation = () => {
    if (!checkIn || !checkOut || !selectedSite) return;

    if (role === null) {
      AppToast.error("로그인이 필요합니다.");
      router.push("/auth/login");
      return;
    }

    if (role !== "USER") {
      AppToast.error("사용자만 예약이 가능합니다.");
      return;
    }

    sessionStorage.setItem(
      "reservationDraft",
      JSON.stringify({
        checkIn,
        checkOut,
        site: selectedSite,
        campingId: camping.id,
      })
    );
  
    router.push(`/campings/${camping.id}/reservation`);

    // router.push(
    //   `/campings/${camping.id}/reservation?checkIn=${checkIn}&checkOut=${checkOut}&siteId=${selectedSite.id}&siteName=${encodeURIComponent(selectedSite.name)}`
    // );
  };

  return (
    <aside>
      <div className="sticky top-24 bg-white rounded-3xl shadow-xl p-6">

        {/* 가격 */}
        <div className="relative overflow-hidden rounded-2xl p-6 mb-6 bg-gradient-to-br from-orange-500 to-orange-400 text-white shadow-lg">
          <div className="text-xs opacity-80">지금 예약 시 최저가</div>

          <div className="text-4xl font-extrabold mt-1">
            ₩
            {camping.sites?.length > 0
              ? Math.min(...camping.sites.map((s) => s.price)).toLocaleString()
              : "0"}
          </div>

          <div className="text-xs mt-2 opacity-90">
            선택한 구역에 따라 금액이 달라집니다
          </div>
        </div>

        {/* 날짜 */}
        <div className="space-y-3">
          <input
            type="date"
            value={checkIn}
            min={today}
            onChange={(e) => {
              const v = e.target.value;
              setCheckIn(v);

              if (checkOut && checkOut <= v) {
                setCheckOut("");
                setAvailableSites([]);
                setSelectedSite(null);
                return;
              }

              if (checkOut) fetchAvailableSites(v, checkOut);
            }}
            className="w-full border rounded-xl px-3 py-2"
          />

          <input
            type="date"
            value={checkOut}
            min={getMinCheckOut(checkIn)}
            onChange={(e) => {
              const v = e.target.value;
              setCheckOut(v);

              if (checkIn) fetchAvailableSites(checkIn, v);
            }}
            className="w-full border rounded-xl px-3 py-2"
          />
        </div>

        {/* 박 수 표시 */}
        {checkIn && checkOut && (
          <div className="mt-2 text-sm text-gray-600">
            총 <b>{nights}박</b> 선택됨
          </div>
        )}

        {/* loading */}
        {loading && (
          <div className="mt-4 text-sm text-gray-500">
            예약 가능 구역 조회 중...
          </div>
        )}

        {/* 사이트 */}
        {availableSites.length > 0 && (
          <div className="mt-6">
            <div className="text-sm font-semibold mb-2">
              {nights}박 기준 예약 가능 구역
            </div>

            <div className="space-y-2">
              {availableSites.map((site: any) => {
                const selected = selectedSite?.id === site.id;

                return (
                  <button
                    key={site.id}
                    onClick={() => setSelectedSite(site)}
                    className={`w-full p-4 rounded-2xl border text-left transition-all
                      ${selected
                        ? "border-orange-500 bg-orange-50 shadow-md scale-[1.02]"
                        : "border-gray-200 hover:bg-gray-50"
                      }`}
                  >
                    <div className="flex justify-between">
                      <div className="font-semibold">{site.name}</div>

                      <div className="font-bold text-orange-600">
                        ₩{site.price.toLocaleString()} / 1박
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* 선택 요약 */}
        {selectedSite && checkIn && checkOut && (
          <div className="mt-6 p-4 rounded-2xl bg-gray-50 border">
            <div className="font-semibold">{selectedSite.name}</div>

            <div className="text-sm text-gray-600 mt-1">
              {checkIn} ~ {checkOut} ({nights}박)
            </div>

            <div className="text-lg font-bold text-orange-600 mt-2">
              ₩{totalPrice.toLocaleString()}
            </div>
          </div>
        )}

        {/* 버튼 */}
        <button
          onClick={() => {
            if (!checkIn || !checkOut)
              return AppToast.error("날짜 선택은 필수입니다.");
            if (!selectedSite)
              return AppToast.error("구역 선택은 필수입니다.");
            setConfirmOpen(true);
          }}
          className="mt-6 w-full bg-orange-500 hover:bg-orange-600 text-white py-4 rounded-2xl font-bold"
        >
          예약하기
        </button>
      </div>

      {/* 모달 */}
      {confirmOpen && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-white w-[340px] rounded-3xl p-6">

            <div className="text-lg font-bold mb-4">
              예약 확인
            </div>

            <div className="space-y-2 text-sm">
              <div>구역: <b>{selectedSite?.name}</b></div>
              <div>{checkIn} ~ {checkOut}</div>
              <div><b>{nights}박</b></div>

              <div className="text-lg font-bold text-orange-500 mt-2">
                ₩{totalPrice.toLocaleString()}
              </div>
            </div>

            <div className="flex gap-2 mt-6">
              <button
                onClick={() => setConfirmOpen(false)}
                className="flex-1 border rounded-xl py-2"
              >
                취소
              </button>

              <button
                onClick={handleFinalReservation}
                className="flex-1 bg-orange-500 text-white rounded-xl py-2 font-semibold"
              >
                예약 확정
              </button>
            </div>
          </div>
        </div>
      )}
    </aside>
  );
}