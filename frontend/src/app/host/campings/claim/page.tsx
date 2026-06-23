"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { claimCamping } from "@/lib/api/host";

export default function CampingClaimPage() {
    const router = useRouter();

    const [contentId, setContentId] = useState("");
    const [tourNum, setTourNum] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setErrorMessage("");

        if (!contentId || !tourNum.trim()) {
            setErrorMessage("contentId와 관광사업자번호를 모두 입력해 주세요.");
            return;
        }

        setIsSubmitting(true);

        try {
            await claimCamping({
                contentId: Number(contentId),
                tourNum: tourNum.trim(),
            });

            alert("캠핑장 소유권 인증이 완료되었습니다.");
            router.push("/host/campings");
        } catch {
            setErrorMessage("캠핑장 소유권 인증에 실패했습니다.");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">내 캠핑장 찾기</h1>
                <p className="mt-2 text-sm text-gray-500">
                    고캠핑에 등록된 캠핑장을 관광사업자번호로 인증해 내 캠핑장으로 등록합니다.
                </p>
            </div>

            <form
                onSubmit={handleSubmit}
                className="space-y-5 rounded-3xl border border-gray-100 bg-white p-8 shadow-sm"
            >
                <div>
                    <label className="mb-2 block text-sm font-medium text-gray-700">
                        Content ID
                    </label>
                    <input
                        type="number"
                        value={contentId}
                        onChange={(event) => setContentId(event.target.value)}
                        className="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-[#3F6B3F]"
                        placeholder="예: 12345"
                    />
                </div>

                <div>
                    <label className="mb-2 block text-sm font-medium text-gray-700">
                        관광사업자번호
                    </label>
                    <input
                        value={tourNum}
                        onChange={(event) => setTourNum(event.target.value)}
                        className="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-[#3F6B3F]"
                        placeholder="관광사업자번호를 입력해 주세요"
                    />
                </div>

                {errorMessage && (
                    <p className="text-sm text-red-500">{errorMessage}</p>
                )}

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full rounded-xl bg-[#3F6B3F] px-4 py-3 text-sm font-semibold text-white disabled:opacity-50"
                >
                    {isSubmitting ? "인증 중..." : "소유권 인증하기"}
                </button>
            </form>
        </div>
    );
}