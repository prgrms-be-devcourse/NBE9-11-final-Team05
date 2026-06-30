"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { claimCamping, searchClaimableCampings } from "@/lib/api/host";
import type { CampingClaimSearchItem } from "@/types/host";
import { AppToast } from "@/lib/ui/toast";

export default function CampingClaimPage() {
  const router = useRouter();

  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<CampingClaimSearchItem[]>([]);
  const [selectedCamping, setSelectedCamping] =
    useState<CampingClaimSearchItem | null>(null);
  const [tourNum, setTourNum] = useState("");

  const [isSearching, setIsSearching] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setSelectedCamping(null);

    if (!keyword.trim()) {
      setErrorMessage("캠핑장 이름을 입력해 주세요.");
      return;
    }

    setIsSearching(true);

    try {
      const data = await searchClaimableCampings(keyword.trim());
      setResults(data);

      if (data.length === 0) {
        setErrorMessage("검색 결과가 없습니다.");
      }
    } catch {
      setErrorMessage("캠핑장 검색에 실패했습니다.");
    } finally {
      setIsSearching(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");

    if (!selectedCamping) {
      setErrorMessage("소유권을 인증할 캠핑장을 선택해 주세요.");
      return;
    }

    if (!tourNum.trim()) {
      setErrorMessage("관광사업자번호를 입력해 주세요.");
      return;
    }

    setIsSubmitting(true);

    try {
      await claimCamping({
        campingId: selectedCamping.campingId,
        tourNum: tourNum.trim(),
      });

      AppToast.success("캠핑장 소유권 인증이 완료되었습니다.");
      router.push("/host/campings");
    } catch {
      setErrorMessage("캠핑장 소유권 인증에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">내 캠핑장 찾기</h1>
        <p className="mt-2 text-sm text-gray-500">
          고캠핑에 등록된 캠핑장을 검색한 뒤 관광사업자번호로 소유권을 인증합니다.
        </p>
      </div>

      <form
        onSubmit={handleSearch}
        className="rounded-3xl border border-gray-100 bg-white p-8 shadow-sm"
      >
        <label className="mb-2 block text-sm font-medium text-gray-700">
          캠핑장 이름 검색
        </label>

        <div className="flex gap-3">
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            className="flex-1 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-[#3F6B3F]"
            placeholder="예: 강릉 오션 캠핑장"
          />

          <button
            type="submit"
            disabled={isSearching}
            className="rounded-xl bg-[#D17A2F] px-5 py-3 text-sm font-semibold text-white disabled:opacity-50"
          >
            {isSearching ? "검색 중..." : "검색"}
          </button>
        </div>
      </form>

      {results.length > 0 && (
        <section className="space-y-3">
          {results.map((camping) => (
            <button
              key={camping.campingId}
              type="button"
              onClick={() => setSelectedCamping(camping)}
              className={`w-full rounded-2xl border bg-white p-5 text-left shadow-sm transition ${
                selectedCamping?.campingId === camping.campingId
                  ? "border-[#3F6B3F]"
                  : "border-gray-100 hover:border-gray-300"
              }`}
            >
              <p className="font-semibold text-gray-900">{camping.name}</p>
              <p className="mt-1 text-sm text-gray-500">
                {camping.region} {camping.city}
              </p>
              <p className="mt-1 text-sm text-gray-400">{camping.address}</p>
            </button>
          ))}
        </section>
      )}

      <form
        onSubmit={handleSubmit}
        className="space-y-5 rounded-3xl border border-gray-100 bg-white p-8 shadow-sm"
      >
        <div>
          <label className="mb-2 block text-sm font-medium text-gray-700">
            선택한 캠핑장
          </label>
          <div className="rounded-xl bg-gray-50 px-4 py-3 text-sm text-gray-600">
            {selectedCamping ? selectedCamping.name : "캠핑장을 먼저 선택해 주세요."}
          </div>
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

        {errorMessage && <p className="text-sm text-red-500">{errorMessage}</p>}

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