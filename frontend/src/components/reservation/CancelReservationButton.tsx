"use client";

import { useActionState, useState } from "react";
import { useRouter } from "next/navigation";
import { cancelReservationAction, type CancelReservationState } from "@/lib/actions/cancelReservation";
import Button from "@/components/ui/Button";

interface Props {
  reservationId: number;
}

const initialState: CancelReservationState = {};

export default function CancelReservationButton({ reservationId }: Props) {
  const router = useRouter();
  const [showConfirm, setShowConfirm] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);

  const boundAction = cancelReservationAction.bind(null, reservationId);
  const [state, formAction, isPending] = useActionState(
    async (prevState: CancelReservationState, _formData: FormData) => {
      const result = await boundAction(prevState, _formData);
      if (result.success) {
        setShowConfirm(false);
        setShowSuccess(true);
      }
      return result;
    },
    initialState
  );

  const handleSuccessClose = () => {
    setShowSuccess(false);
    // 팝업 닫을 때 페이지 새로고침 — revalidatePath 대신 사용
    router.refresh();
  };

  return (
    <>
      {/* 취소하기 버튼 */}
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

            {state.error && (
              <p className="mb-4 text-center text-xs text-red-600">{state.error}</p>
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
              <form action={formAction} className="flex-1">
                <button
                  type="submit"
                  disabled={isPending}
                  className="w-full rounded-full border border-red-200 bg-transparent px-6 py-3 text-sm font-semibold text-red-600 transition-colors hover:bg-white hover:text-red-700 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {isPending ? "취소 중..." : "예약 취소"}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* 취소 완료 모달 — 사용자가 직접 닫아야 함 */}
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