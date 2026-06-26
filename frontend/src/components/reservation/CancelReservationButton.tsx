"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { cancelReservationAction, type CancelReservationState } from "@/lib/actions/cancelReservation";
import Button from "@/components/ui/Button";

interface Props {
  reservationId: number;
}

export default function CancelReservationButton({ reservationId }: Props) {
  const router = useRouter();
  const [showConfirm, setShowConfirm] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCancel = async () => {
    setIsPending(true);
    setError(null);
    try {
      const result: CancelReservationState = await cancelReservationAction(reservationId, {});
      if (result.success) {
        setShowConfirm(false);
        setShowSuccess(true);
      } else {
        setError(result.error ?? "예약 취소 중 오류가 발생했습니다.");
      }
    } catch {
      setError("예약 취소 중 오류가 발생했습니다.");
    } finally {
      setIsPending(false);
    }
  };

  const handleSuccessClose = () => {
    setShowSuccess(false);
    router.refresh();
  };

  return (
    <>
      <button
        onClick={() => setShowConfirm(true)}
        className="w-full rounded-full border border-red-200 bg-transparent px-6 py-3 text-sm font-semibold text-red-600 transition-colors hover:bg-white hover:text-red-700"
      >
        예약을 취소하고 싶어요
      </button>

      {/* 취소 확인 모달 */}
      {showConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="mx-4 w-full max-w-sm rounded-3xl bg-white p-6 shadow-xl">
            <h2 className="mb-2 text-center text-lg font-bold text-stone-900">
              예약을 취소하시겠어요?
            </h2>
            <p className="mb-6 text-center text-sm text-stone-500">
              정말로 취소하겠습니까?
              <br />
              <span className="text-xs text-red-400">취소 후에는 되돌릴 수 없습니다.</span>
            </p>

            {error && (
              <p className="mb-4 text-center text-xs text-red-600">{error}</p>
            )}

            <div className="flex gap-3">
              <Button
                variant="ghost"
                fullWidth
                onClick={() => setShowConfirm(false)}
                disabled={isPending}
              >
                돌아가기
              </Button>
              <button
                onClick={handleCancel}
                disabled={isPending}
                className="flex-1 rounded-full border border-red-200 bg-transparent px-6 py-3 text-sm font-semibold text-red-600 transition-colors hover:bg-white hover:text-red-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isPending ? "취소 중..." : "예약 취소"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 취소 완료 모달 */}
      {showSuccess && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="mx-4 w-full max-w-sm rounded-3xl bg-white p-6 shadow-xl text-center">
            <div className="mb-4 flex items-center justify-center">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-stone-100 text-2xl">
                ✓
              </div>
            </div>
            <h2 className="mb-2 text-lg font-bold text-stone-900">
              취소가 완료되었습니다.
            </h2>
            <p className="mb-6 text-sm text-stone-500">
              예약이 정상적으로 취소되었습니다.
            </p>
            <Button fullWidth onClick={handleSuccessClose}>
              확인
            </Button>
          </div>
        </div>
      )}
    </>
  );
}
