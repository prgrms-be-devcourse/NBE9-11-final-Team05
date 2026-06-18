"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { signupApi } from "@/lib/api/signup";
import { useSignupStore } from "@/stores/signupStore";

export default function SignupStep3() {
  const router = useRouter();

  const [businessNumber, setBusinessNumber] = useState("");
  const [campingName, setCampingName] = useState("");
  const [location, setLocation] = useState("");
  const [detailAddress, setDetailAddress] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const {
    email,
    password,
    name,
    nickname,
    phone,
    reset,
  } = useSignupStore();

  const handleAddressSearch = () => {
    // TODO: 카카오 우편번호 API 연동
    alert("주소 검색 API 연동 예정");
  };

  useEffect(() => {
    if (!email) {
      router.replace("/auth/signup/step1");
    }
  }, [email, router]);

  const handleSubmit = async () => {
    setError("");
    setLoading(true);
  
    try {
      if (!email) {
        router.push("/auth/signup/step1");
        return;
      }
  
      const address = detailAddress
        ? `${location} ${detailAddress}`
        : location;
  
      await signupApi.signupHost({
        email,
        password,
        name,
        nickname,
        phone,
        businessNum: businessNumber,
        campingName,
        address,
      });
  
      // Zustand 데이터 초기화
      reset();
  
      sessionStorage.removeItem("signupRole");
  
      router.push("/auth/login");
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "호스트 등록에 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  const isFormValid = businessNumber !== "" && campingName !== "" && location !== "";

  return (
    <div className="min-h-screen flex flex-col bg-white">
      {/* 헤더 */}
      <header className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
        <h1 className="text-xl font-bold text-gray-900 tracking-tight">캠핑가잣</h1>
        <button aria-label="메뉴 열기" className="flex flex-col gap-1.5 p-2 hover:opacity-70 transition-opacity">
          <span className="block w-6 h-0.5 bg-gray-800" />
          <span className="block w-6 h-0.5 bg-gray-800" />
          <span className="block w-6 h-0.5 bg-gray-800" />
        </button>
      </header>

      {/* 본문 */}
      <main className="flex-1 flex items-center justify-center px-6 py-10">
        <div className="flex flex-col md:flex-row items-center gap-10 w-full max-w-2xl">
          {/* 왼쪽 안내 문구 */}
          <div className="shrink-0">
            <h2 className="text-2xl font-bold text-gray-800 leading-snug">
              Host 정보를<br />입력해주세요
            </h2>
          </div>

          {/* 오른쪽 폼 카드 */}
          <div className="w-full max-w-sm bg-[#5C7A5C] rounded-2xl px-7 py-7 flex flex-col gap-4">
            {/* 사업자 번호 */}
            <div className="flex items-center gap-3">
              <label className="text-white text-sm w-20 shrink-0 text-right">사업자 번호</label>
              <input
                type="text"
                placeholder="123-45-67890"
                value={businessNumber}
                onChange={(e) => setBusinessNumber(e.target.value)}
                className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
            </div>

            {/* 캠핑장명 */}
            <div className="flex items-center gap-3">
              <label className="text-white text-sm w-20 shrink-0 text-right">캠핑장명</label>
              <input
                type="text"
                placeholder="캠핑장 이름"
                value={campingName}
                onChange={(e) => setCampingName(e.target.value)}
                className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
            </div>

            {/* 위치 + 검색 버튼 */}
            <div className="flex items-center gap-3">
              <label className="text-white text-sm w-20 shrink-0 text-right">위치</label>
              <input
                type="text"
                placeholder="주소 검색"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
              <button
                onClick={handleAddressSearch}
                className="shrink-0 px-3 py-2 bg-[#4a6b4a] hover:bg-[#3d5c3d] text-white text-xs rounded-lg transition-colors"
              >
                검색
              </button>
            </div>

            {/* 상세 주소 */}
            <div className="flex items-center gap-3">
              <div className="w-20 shrink-0" />
              <input
                type="text"
                placeholder="상세 주소"
                value={detailAddress}
                onChange={(e) => setDetailAddress(e.target.value)}
                className="flex-1 px-3 py-2 rounded-lg bg-white/95 text-sm text-gray-700 placeholder-gray-400 outline-none focus:ring-2 focus:ring-orange-400"
              />
            </div>

            {/* 에러 메시지 */}
            {error && <p className="text-red-300 text-xs text-center">{error}</p>}

            {/* 등록 신청 버튼 */}
            <button
              onClick={handleSubmit}
              disabled={!isFormValid || loading}
              className={`w-full mt-2 py-3 rounded-xl text-white font-bold text-base transition-colors
                ${isFormValid && !loading
                  ? "bg-orange-400 hover:bg-orange-500 active:bg-orange-600"
                  : "bg-orange-200 cursor-not-allowed"
                }`}
            >
              {loading ? "처리 중..." : "Host 등록 신청"}
            </button>
          </div>
        </div>
      </main>

      {/* 푸터 */}
      <footer className="px-6 py-6 border-t border-gray-100">
        <div className="max-w-3xl mx-auto flex flex-col md:flex-row md:justify-between gap-3">
          <div>
            <p className="font-semibold text-gray-800 text-sm">캠핑가잣</p>
            <p className="text-gray-500 text-xs mt-1">최고의 캠핑을 소개합니다 어쩌구.. 우리 캠핑<br />사이트 최고</p>
          </div>
          <div className="flex flex-col md:flex-row gap-2 md:gap-8 text-xs text-gray-500">
            <span>연락처: 어쩌구</span>
            <span>메일: 어쩌구@이쩌구.com</span>
          </div>
        </div>
      </footer>
    </div>
  );
}