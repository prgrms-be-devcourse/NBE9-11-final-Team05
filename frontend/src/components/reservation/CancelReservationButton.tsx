"use client";

import { useState } from "react";
import Button from "@/components/ui/Button";

/**
 * 예약 취소 API가 아직 없어서(백엔드 추후 작업), 클릭 시 안내만 표시.
 * API 준비되면 이 컴포넌트 안에서 cancelReservation(reservationId) 호출로 교체.
 */
export default function CancelReservationButton() {
  const [showNotice, setShowNotice] = useState(false);

  return (
    <div>
      <Button
        variant="danger-outline"
        fullWidth
        onClick={() => setShowNotice(true)}
      >
        예약을 취소하고 싶어요
      </Button>
      {showNotice && (
        <p className="mt-2 rounded-xl bg-stone-50 px-4 py-2 text-center text-sm text-stone-500">
          예약 취소 기능은 준비 중입니다. 빠르게 지원할 예정이니 잠시만 기다려주세요.
        </p>
      )}
    </div>
  );
}
