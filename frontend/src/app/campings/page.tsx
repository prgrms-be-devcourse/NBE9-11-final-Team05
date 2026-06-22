import SearchBox from "@/components/search/SearchBox";
import { getCampings } from "@/lib/api/camping";
import Link from "next/link";

export default async function CampingListPage({
  searchParams,
}: {
    searchParams: Promise<{ 
        keyword?: string; 
        page?: string 
    }>;
}) {
    const resolved = await searchParams;

    const keyword = resolved?.keyword ?? "";
    const page = Number(resolved?.page ?? 0);

    const data = await getCampings(keyword, page);

    const campings = data.content;
    const totalPages = data.totalPages;

    const pageWindow = 2;

    const start = Math.max(0, page - pageWindow);
    const end = Math.min(totalPages - 1, page + pageWindow);

    const pages = Array.from(
    { length: end - start + 1 },
    (_, i) => start + i
    );

  return (
    <div className="max-w-6xl mx-auto py-12 px-4">
      {/* 헤더 */}
      <div className="mb-10">
        <h1 className="text-3xl font-bold text-gray-800">
          캠핑장 검색
        </h1>
        <p className="text-gray-500 mt-1">
          원하는 캠핑장을 빠르게 찾아보세요
        </p>
      </div>

      {/* 검색 영역 카드 */}
      <div className="mb-10 bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-800">
            검색
          </h2>

          {keyword && (
            <span className="text-sm text-gray-500 bg-gray-100 px-3 py-1 rounded-full">
              "{keyword}"
            </span>
          )}
        </div>

        <SearchBox initialKeyword={keyword} />
      </div>

      {/* 결과 상태 */}
      {keyword && (
        <p className="text-gray-500 mb-6">
          "{keyword}" 검색 결과
        </p>
      )}

      {/* 리스트 */}
      <div className="grid md:grid-cols-3 gap-6">
        {campings.map((camping) => (
            <Link
            key={camping.id}
            href={`/campings/${camping.id}`}
            className="block"
            >
            <div
                className="bg-white rounded-2xl shadow-sm border border-gray-100
                overflow-hidden
                hover:shadow-lg
                transition
                cursor-pointer
                "
            >
                <div className="h-44 overflow-hidden">
                <img
                    src={
                    camping.firstImageUrl || "/images/default-camping.png"
                    }
                    className="w-full h-full object-cover hover:scale-105 transition duration-300"
                />
                </div>

                <div className="p-4">
                <h3 className="font-semibold text-lg text-gray-800">
                    {camping.name}
                </h3>

                <p className="text-sm text-gray-500 mt-1">
                    {camping.address}
                </p>
                </div>
            </div>
            </Link>
        ))}
        </div>

        {/* 페이지네이션 */}
        <div className="flex justify-center items-center gap-2 mt-10">
            {/* 이전 */}
            <Link
                href={
                    page > 0
                    ? `/campings?keyword=${keyword}&page=${page - 1}`
                    : "#"
                }
                className={`px-3 py-2 border rounded ${
                    page === 0 ? "pointer-events-none opacity-30" : ""
                }`}
                >
                이전
            </Link>

            {/* 첫 페이지 */}
            {start > 0 && (
                <>
                <Link
                    href={`/campings?keyword=${keyword}&page=0`}
                    className="px-3 py-2 border rounded"
                >
                    1
                </Link>
                <span className="px-2">...</span>
                </>
            )}

            {/* 페이지 번호 */}
            {pages.map((p) => (
                <Link
                key={p}
                href={`/campings?keyword=${keyword}&page=${p}`}
                className={`px-3 py-2 border rounded ${
                    p === page ? "bg-[#4B6945] text-white" : ""
                }`}
                >
                {p + 1}
                </Link>
            ))}

            {/* 마지막 페이지 */}
            {end < totalPages - 1 && (
                <>
                <span className="px-2">...</span>
                <Link
                    href={`/campings?keyword=${keyword}&page=${totalPages - 1}`}
                    className="px-3 py-2 border rounded"
                >
                    {totalPages}
                </Link>
                </>
            )}

            {/* 다음 */}
            <Link
                href={
                    page + 1 < totalPages
                    ? `/campings?keyword=${keyword}&page=${page + 1}`
                    : "#"
                }
                className={`px-3 py-2 border rounded ${
                    page + 1 >= totalPages ? "pointer-events-none opacity-30" : ""
                }`}
                >
                다음
            </Link>
        </div>
    </div>
  );
}