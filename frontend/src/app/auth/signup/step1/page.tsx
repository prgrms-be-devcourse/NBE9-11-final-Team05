"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

type Role = "user" | "host";

export default function SignupStep1() {
  const [selected, setSelected] = useState<Role | null>(null);
  const router = useRouter();

  const handleNext = () => {
    if (!selected) return;
    // 선택한 role을 sessionStorage에 저장해서 step2/3에서 사용
    sessionStorage.setItem("signupRole", selected);
    router.push("/auth/signup/step2");
  };

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
      <main className="flex-1 flex flex-col items-center justify-center px-6 py-12 gap-10">
        <div className="flex gap-6">
          {/* Guest 카드 */}
          <button
            onClick={() => setSelected("user")}
            className={`relative w-52 h-52 rounded-2xl overflow-hidden transition-all duration-200 focus:outline-none
              ${selected === "user" ? "ring-4 ring-orange-400 scale-105" : "ring-2 ring-transparent hover:scale-105"}`}
          >
            {/* 배경 이미지 */}
            <img
              src="/test.svg"
              alt="Guest 캠핑 이미지"
              className="w-full h-full object-cover"
            />
            {/* fallback 배경 */}
            <div className="absolute inset-0 bg-gradient-to-b from-stone-700 via-stone-800 to-stone-900 -z-10" />
            {/* 라벨 */}
            <div className="absolute bottom-0 left-0 right-0 bg-[#6B8F6B]/90 py-3">
              <span className="text-white text-lg font-bold tracking-wide">User !</span>
            </div>
          </button>

          {/* Host 카드 */}
          <button
            onClick={() => setSelected("host")}
            className={`relative w-52 h-52 rounded-2xl overflow-hidden transition-all duration-200 focus:outline-none
              ${selected === "host" ? "ring-4 ring-orange-400 scale-105" : "ring-2 ring-transparent hover:scale-105"}`}
          >
            <img
              src="/test.svg"
              alt="Host 캠핑 이미지"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-b from-stone-700 via-stone-800 to-stone-900 -z-10" />
            <div className="absolute bottom-0 left-0 right-0 bg-[#6B8F6B]/90 py-3">
              <span className="text-white text-lg font-bold tracking-wide">Host !</span>
            </div>
          </button>
        </div>

        {/* 다음으로 버튼 */}
        <button
          onClick={handleNext}
          disabled={!selected}
          className={`px-16 py-4 rounded-full text-white text-lg font-bold tracking-wide transition-all duration-200
            ${selected
              ? "bg-orange-400 hover:bg-orange-500 active:bg-orange-600 shadow-md hover:shadow-lg"
              : "bg-orange-200 cursor-not-allowed"
            }`}
        >
          다음으로 →
        </button>
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