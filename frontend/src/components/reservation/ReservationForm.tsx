"use client";

import { useState } from "react";
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

  const [rsvName, setRsvName] = useState("");
  const [guestCount, setGuestCount] = useState(1);
  const [rsvPhone, setRsvPhone] = useState("");
  const [request, setRequest] = useState("");
  const [error, setError] = useState<string | null>(null);

  const selectedSite = siteOptions.find(
    (s) => s.id === Number(defaultSiteId)
  );

  const maxCapacity = selectedSite?.maxCapacity ?? 0;

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

  const validate = () => {
    if (!rsvName.trim()) {
      setError("예약자 성함은 필수입니다.");
      return false;
    }

    if (!rsvPhone.trim()) {
      setError("예약자 번호는 필수입니다.");
      return false;
    }

    if (guestCount < 1) {
      setError("예약 인원은 1명 이상이어야 합니다.");
      return false;
    }

    if (guestCount > maxCapacity) {
      setError(`최대 ${maxCapacity}명까지 예약 가능합니다.`);
      return false;
    }

    setError(null);
    return true;
  };

  return (
    <Card className="mx-auto max-w-xl">
      <h1 className="mb-6 text-xl font-bold text-stone-900">
        예약 확정
      </h1>

      {/* 고정 정보 */}
      <div className="rounded-xl border bg-stone-50 p-4 text-sm space-y-2 mb-4">
        <div>
          기간:{" "}
          <b>
            {defaultCheckIn} ~ {defaultCheckOut} ({nights}박)
          </b>
        </div>

        <div>
          구역: <b>{selectedSite?.name}</b>
        </div>

        <div>
          최대 인원: <b>{maxCapacity}명</b>
        </div>

        {estimatedPrice !== null && (
          <div>
            금액:{" "}
            <b className="text-orange-600">
              {estimatedPrice.toLocaleString()}원
            </b>
          </div>
        )}
      </div>

      {/* 🔥 핵심: formAction + onSubmit 같이 사용 */}
      <form
        action={formAction}
        onSubmit={(e) => {
          if (!validate()) {
            e.preventDefault();
          }
        }}
        className="flex flex-col gap-5"
      >
        <Input
          name="rsvName"
          label="예약자 성함"
          placeholder="예약자 성함을 입력해주세요."
          value={rsvName}
          onChange={(e) => setRsvName(e.target.value)}
          autoFocus
        />

        <Input
          type="number"
          name="guestCount"
          label={`예약 인원 (최대 ${maxCapacity}명)`}
          min={1}
          max={maxCapacity}
          value={guestCount}
          onChange={(e) => setGuestCount(Number(e.target.value))}
        />

        <Input
          name="rsvPhone"
          label="예약자 전화번호"
          placeholder="010-1234-5678"
          value={rsvPhone}
          onChange={(e) => setRsvPhone(e.target.value)}
        />

        <Textarea
          name="request"
          label="요청 사항"
          rows={3}
          placeholder="요청사항이 있다면 입력해주세요"
          value={request}
          onChange={(e) => setRequest(e.target.value)}
        />

        {/* hidden */}
        <input type="hidden" name="checkIn" value={defaultCheckIn} />
        <input type="hidden" name="checkOut" value={defaultCheckOut} />
        <input type="hidden" name="siteId" value={defaultSiteId} />

        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" required />
          약관 동의
        </label>

        {error && (
          <p className="text-red-500 text-sm">{error}</p>
        )}

        {state.error && (
          <p className="text-red-500 text-sm">
            {state.error}
          </p>
        )}

        <Button type="submit" disabled={isPending} fullWidth>
          {isPending ? "처리 중..." : "결제하기"}
        </Button>
      </form>
    </Card>
  );
}