"use client";

import { useActionState } from "react";
import {
  submitReservation,
  type ReservationFormState,
} from "@/lib/actions/reservation";
import Input from "@/components/ui/Input";
import Textarea from "@/components/ui/Textarea";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

interface SiteOption {
  id: number;
  name: string;
  price: number;
  baseCapacity: number;
  maxCapacity: number;
}

interface ReservationFormProps {
  campingId: string;
  siteOptions: SiteOption[];
  campingName?: string;
  defaultCheckIn: string;
  defaultCheckOut: string;
  defaultSiteId: number;
}

const initialState: ReservationFormState = {};

export default function ReservationForm({
  campingId,
  siteOptions,
  campingName,
  defaultCheckIn,
  defaultCheckOut,
  defaultSiteId,
}: ReservationFormProps) {
  const boundAction = submitReservation.bind(null, campingId);

  const [state, formAction, isPending] = useActionState(
    boundAction,
    initialState
  );

  // 🔥 여기 핵심
  const selectedSite = siteOptions.find(
    (s) => s.id === Number(defaultSiteId)
  );

  const nights = Math.max(
    0,
    Math.floor(
      (new Date(defaultCheckOut).getTime() -
        new Date(defaultCheckIn).getTime()) /
        (1000 * 60 * 60 * 24)
    )
  );

    const estimatedPrice =
        selectedSite && nights > 0
            ? selectedSite.price * nights
            : null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSiteId || !rsvName || !checkIn || !checkOut) {
      setError("필수 입력값을 확인해주세요.");
      return;
    }
    if (selectedSite && guestCount > selectedSite.maxCapacity) {
      setError(`이 구역은 최대 ${selectedSite.maxCapacity}명까지 예약 가능합니다.`);
      return;
    }
    if (!agreed) {
      setError("약관에 동의해주세요.");
      return;
    }

    setError(null);
    setIsPending(true);
    try {
      const reservation = await createReservationClient({
        siteId: selectedSiteId,
        rsvName,
        rsvPhone,
        checkIn,
        checkOut,
        guestCount,
        request: request || undefined,
      });
      router.push(`/campings/${campingId}/reservation/detail?reservationId=${reservation.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "예약 생성 중 오류가 발생했습니다.");
    } finally {
      setIsPending(false);
    }
  };

  return (
    <Card className="mx-auto max-w-xl">
        <h1 className="mb-6 text-xl font-bold text-stone-900">
        예약 확정
        </h1>

        {/* 🔥 고정 정보 확실히 보이게 */}
        <div className="rounded-xl border bg-stone-50 p-4 text-sm space-y-2 mb-4">
            <div>
              기간:{" "}
                  <b>
                    {defaultCheckIn} ~ {defaultCheckOut} ({nights}박)
                  </b>
            </div>
          <div>
              구역: <b>{selectedSite?.name ?? "로딩 실패"}</b>
          </div>

          <div>
              금액:{" "}
                  <b className="text-orange-600">
                      {estimatedPrice
                          ? `${estimatedPrice.toLocaleString()}원`
                          : "계산 중"}
                  </b>
          </div>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-5">
        <Input
          name="rsvName"
          label="예약자 성함"
          placeholder="이름을 입력해주세요"
          value={rsvName}
          onChange={(e) => setRsvName(e.target.value)}
          required
        />

        <div className="flex flex-col gap-1.5">
          <Input
            type="number"
            name="guestCount"
            label="예약 인원"
            min={1}
            max={selectedSite?.maxCapacity}
            value={guestCount}
            defaultValue={1}
            onChange={(e) => setGuestCount(Number(e.target.value))}
            required
          />
          {selectedSite && (
            <p className="text-xs text-stone-400">
              기준 {selectedSite.baseCapacity}명 / 최대 {selectedSite.maxCapacity}명
            </p>
          )}
        </div>

        <Input
          name="rsvPhone"
          label="예약자 번호"
          placeholder="010-1234-5678"
          value={rsvPhone}
          onChange={(e) => setRsvPhone(e.target.value)}
          required
        />

        <Textarea
          name="request"
          label="요청 사항"
          rows={3}
          placeholder="요청사항이 있다면 입력해주세요"
          value={request}
          onChange={(e) => setRequest(e.target.value)}
        />

        {/* 🔥 hidden 유지 */}
        <input
          type="hidden"
          name="checkIn"
          value={defaultCheckIn}
        />
        <input
          type="hidden"
          name="checkOut"
          value={defaultCheckOut}
        />
        <input
          type="hidden"
          name="siteId"
          value={defaultSiteId}
        />

        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" required />
          약관 동의
        </label>

        {error && (
          <p className="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{error}</p>
        )}

        {estimatedPrice !== null && (
          <div className="flex items-center justify-end gap-2 border-t border-stone-100 pt-4 text-base">
            <span className="text-stone-500">결제금액:</span>
            <span className="font-bold text-stone-900">{estimatedPrice.toLocaleString()}원</span>
          </div>
        )}

        <Button type="submit" disabled={isPending} fullWidth>
          {isPending ? "처리 중..." : "결제하기"}
        </Button>
      </form>
    </Card>
  );
}
