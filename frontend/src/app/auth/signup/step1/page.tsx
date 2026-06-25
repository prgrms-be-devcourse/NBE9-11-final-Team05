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

      {/* 본문 */}
      <main className="flex-1 flex flex-col items-center justify-center px-6 py-12 gap-10 -mt-60">
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
    </div>
  );
}