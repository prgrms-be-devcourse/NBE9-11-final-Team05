"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createReservationClient } from "@/lib/api/reservation.client";
import Input from "@/components/ui/Input";
import Select from "@/components/ui/Select";
import Textarea from "@/components/ui/Textarea";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

interface SiteOption {
  id: number;
  name: string;
  price: number;
}

interface ReservationFormProps {
  campingId: string;
  siteOptions: SiteOption[];
  campingName?: string;
  defaultCheckIn?: string;
  defaultCheckOut?: string;
}

export default function ReservationForm({
  campingId,
  siteOptions,
  campingName,
  defaultCheckIn,
  defaultCheckOut,
}: ReservationFormProps) {
  const router = useRouter();
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [selectedSiteId, setSelectedSiteId] = useState<number | null>(null);
  const [checkIn, setCheckIn] = useState(defaultCheckIn ?? "");
  const [checkOut, setCheckOut] = useState(defaultCheckOut ?? "");
  const [rsvName, setRsvName] = useState("");
  const [rsvPhone, setRsvPhone] = useState("");
  const [guestCount, setGuestCount] = useState(1);
  const [request, setRequest] = useState("");
  const [agreed, setAgreed] = useState(false);

  const selectedSite = siteOptions.find((s) => s.id === selectedSiteId);
  const nights =
    checkIn && checkOut
      ? Math.max(
          0,
          Math.floor(
            (new Date(checkOut).getTime() - new Date(checkIn).getTime()) /
              (1000 * 60 * 60 * 24)
          )
        )
      : 0;
  const estimatedPrice = selectedSite && nights > 0 ? selectedSite.price * nights : null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSiteId || !rsvName || !checkIn || !checkOut) {
      setError("필수 입력값을 확인해주세요.");
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
      {campingName && <p className="mb-1 text-sm text-stone-500">{campingName}</p>}
      <h1 className="mb-6 text-xl font-bold text-stone-900">예약하기</h1>

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
          <span className="text-sm font-medium text-stone-700">예약일</span>
          <div className="flex items-center gap-3">
            <input
              type="date"
              name={!!defaultCheckIn ? undefined : "checkIn"}
              required={!defaultCheckIn}
              value={checkIn}
              onChange={(e) => setCheckIn(e.target.value)}
              disabled={!!defaultCheckIn}
              className={`flex-1 rounded-full border border-stone-200 bg-white px-4 py-2.5 text-sm text-stone-800 outline-none transition-colors ${defaultCheckIn ? "bg-stone-50 text-stone-500" : "focus:border-emerald-700 focus:ring-2 focus:ring-emerald-100"}`}
            />
            <span className="text-stone-400">~</span>
            <input
              type="date"
              name={!!defaultCheckOut ? undefined : "checkOut"}
              required={!defaultCheckOut}
              value={checkOut}
              onChange={(e) => setCheckOut(e.target.value)}
              disabled={!!defaultCheckOut}
              className={`flex-1 rounded-full border border-stone-200 bg-white px-4 py-2.5 text-sm text-stone-800 outline-none transition-colors ${defaultCheckOut ? "bg-stone-50 text-stone-500" : "focus:border-emerald-700 focus:ring-2 focus:ring-emerald-100"}`}
            />
          </div>
          {!!defaultCheckIn && <input type="hidden" name="checkIn" value={checkIn} />}
          {!!defaultCheckOut && <input type="hidden" name="checkOut" value={checkOut} />}
          {nights > 0 && <p className="text-xs text-stone-400">{nights}박</p>}
        </div>

        <Select
          name="siteId"
          label="구역선택"
          required
          defaultValue=""
          onChange={(e) => setSelectedSiteId(Number(e.target.value))}
        >
          <option value="" disabled>구역을 선택해주세요</option>
          {siteOptions.map((site) => (
            <option key={site.id} value={site.id}>{site.name}</option>
          ))}
        </Select>

        <Input
          type="number"
          name="guestCount"
          label="예약 인원"
          min={1}
          value={guestCount}
          onChange={(e) => setGuestCount(Number(e.target.value))}
          required
        />

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

        <label className="flex items-center gap-2 text-sm text-stone-600">
          <input
            type="checkbox"
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
            className="h-4 w-4 rounded border-stone-300 text-emerald-700 focus:ring-emerald-600"
          />
          위 약관에 동의합니다.
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