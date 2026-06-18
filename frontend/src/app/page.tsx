import Link from "next/link";
import { getLatestCampings } from "./lib/camping";

export default async function Home() {
  const campings = await getLatestCampings();
  
  return (
    <>
      <section className="relative overflow-hidden rounded-[32px] bg-[#F6F8F4] py-16">
        {/* 배경 */}
        <div className="absolute -top-20 -left-20 w-72 h-72 rounded-full bg-[#4B6945]/10 blur-3xl" />

        <div className="absolute -bottom-32 -right-20 w-96 h-96 rounded-full bg-[#4B6945]/10 blur-3xl" />

        <div className="relative max-w-4xl mx-auto text-center">

          <p className="text-[#4B6945] font-semibold mb-3">
            국내 캠핑장 통합 검색
          </p>

          <h1 className="text-4xl font-bold leading-tight">
            자연 속에서
            <br />
            특별한 하루를 만나보세요
          </h1>

          <p className="text-gray-500 mt-5 text-base">
            전국 캠핑장을 한 곳에서 검색하고 예약하세요.
          </p>

          {/* 검색창 */}
          <div className="bg-white rounded-3xl shadow-lg mt-10 max-w-3xl mx-auto p-2">
            <form className="flex items-center">
              <input
                className="flex-1 px-6 py-4 outline-none"
                placeholder="지역 또는 캠핑장명을 입력하세요"
              />

              <button
                className="
                  bg-[#4B6945]
                  text-white
                  px-8
                  py-4
                  rounded-2xl
                  font-semibold
                  hover:opacity-90
                  transition
                "
              >
                검색
              </button>
            </form>
          </div>

          {/* 인기 지역 */}
          <div className="flex justify-center gap-3 mt-8 flex-wrap">
            {["강원도", "경기도", "제주도", "부산", "충청도"].map(
              (region) => (
                <button
                  key={region}
                  className="
                    bg-[#4B6945]
                    text-white
                    px-4
                    py-2
                    rounded-full
                    text-sm
                    hover:opacity-90
                  "
                >
                  {region}
                </button>
              )
            )}
          </div>
        </div>
      </section>

      <section className="mt-20">
        <h2 className="text-3xl font-bold mb-8">
          최근 등록된 캠핑장
        </h2>

        <div className="grid md:grid-cols-3 gap-8">
          {campings.map((camping) => (
             <Link
              key={camping.id}
              href={`/campings/${camping.id}`}
              >
                <div
                  className="
                    bg-white
                    rounded-3xl
                    shadow-lg
                    p-4
                    hover:shadow-xl
                    transition
                    cursor-pointer
                  "
                >
                  <div className="h-60 rounded-2xl mb-4 overflow-hidden">
                    <img
                      src={camping.firstImageUrl || "/images/default-camping.png"}
                      alt={camping.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
        
                  <h3 className="font-bold text-xl">
                    {camping.name}
                  </h3>
        
                  <p className="text-gray-500 mt-2">
                    {camping.address}
                  </p>
                </div>
              </Link>
          ))}
        </div>
      </section>
    </>
  );
}