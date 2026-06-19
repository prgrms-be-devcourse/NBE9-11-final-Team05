"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";

export default function SearchBox({
  initialKeyword = "",
}: {
  initialKeyword?: string;
}) {
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);

  const [keyword, setKeyword] = useState(initialKeyword);

  const handleSearch = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();

    router.push(`/campings?keyword=${encodeURIComponent(keyword)}`);
  };

  // 🔥 핵심: 페이지 바뀌면 input 비우고 포커스
  useEffect(() => {
    setKeyword(""); // 값 초기화

    if (inputRef.current) {
      inputRef.current.focus(); // 커서만 유지
    }
  }, [initialKeyword]);

  return (
    <form className="flex items-center" onSubmit={handleSearch}>
      <input
        ref={inputRef}
        className="flex-1 px-6 py-4 outline-none"
        placeholder="지역 또는 캠핑장명을 입력하세요"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
      />

      <button
        type="submit"
        className="bg-[#CC7C35] text-white px-8 py-4 rounded-2xl font-semibold"
      >
        검색
      </button>
    </form>
  );
}