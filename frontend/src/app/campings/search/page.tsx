"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { searchCampings } from "@/lib/api/camping";
import { CampingSearchResponse } from "@/types/camping";

const today = new Date().toISOString().split("T")[0];
const tomorrow = new Date(Date.now() + 86400000).toISOString().split("T")[0];

const MAX_PRICE = 500000;
const STEP = 10000;

export default function CampingSearchPage() {
    const [filterOpen, setFilterOpen] = useState(false);

    const [keyword, setKeyword] = useState("");
    const [checkIn, setCheckIn] = useState(today);
    const [checkOut, setCheckOut] = useState(tomorrow);
    const [guestCount, setGuestCount] = useState(1);
    const [roomCount, setRoomCount] = useState(1);
    const [minPrice, setMinPrice] = useState(0);
    const [maxPrice, setMaxPrice] = useState(MAX_PRICE);

    const [campings, setCampings] = useState<CampingSearchResponse[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);
    const [searched, setSearched] = useState(false);

    useEffect(() => {
        handleSearch(0);
    }, []);

    const handleSearch = async (currentPage = 0) => {
        setFilterOpen(false);
        setLoading(true);
        try {
            const result = await searchCampings({
                keyword: keyword || undefined,
                checkIn,
                checkOut,
                guestCount,
                roomCount,
                minPrice: minPrice > 0 ? minPrice : undefined,
                maxPrice: maxPrice < MAX_PRICE ? maxPrice : undefined,
                page: currentPage,
                size: 10,
            });
            setCampings(result.content);
            setTotalElements(result.totalElements);
            setTotalPages(result.totalPages);
            setPage(currentPage);
            setSearched(true);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    const minPercent = (minPrice / MAX_PRICE) * 100;
    const maxPercent = (maxPrice / MAX_PRICE) * 100;

    return (
        <div className="max-w-7xl mx-auto px-6 py-10">

            {/* 상단 헤더 */}
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-3xl font-bold text-[#2F3A2F]">캠핑장 검색</h1>
                <button
                    onClick={() => setFilterOpen(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-[#5C7A5C] text-white rounded-xl text-sm font-medium hover:bg-[#4a6349] transition"
                >
                    검색 필터
                </button>
            </div>

            {/* 검색 결과 */}
            <div className="flex-1">
                {loading ? (
                    <div className="flex items-center justify-center h-48 text-gray-400">
                        검색 중...
                    </div>
                ) : searched && campings.length === 0 ? (
                    <div className="flex items-center justify-center h-48 text-gray-400">
                        검색 결과가 없습니다.
                    </div>
                ) : !searched ? (
                    <div className="flex items-center justify-center h-48 text-gray-400">
                        필터를 설정하고 검색해보세요.
                    </div>
                ) : (
                    <>
                        <p className="text-sm text-gray-500 mb-4">
                            총 {totalElements.toLocaleString()}개의 캠핑장
                        </p>

                        <div className="flex flex-col gap-4">
                            {campings.map((camping) => (
                                <Link key={camping.id} href={`/campings/${camping.id}`}>
                                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition overflow-hidden flex cursor-pointer">
                                        <div className="w-56 h-44 shrink-0 bg-gray-100">
                                            {camping.images.length > 0 ? (
                                                <img
                                                    src={camping.images[0]}
                                                    alt={camping.name}
                                                    className="w-full h-full object-cover"
                                                />
                                            ) : (
                                                <div className="w-full h-full flex items-center justify-center text-gray-300 text-sm">
                                                    이미지 없음
                                                </div>
                                            )}
                                        </div>

                                        <div className="flex-1 p-5 flex flex-col justify-between">
                                            <div>
                                                <h3 className="font-bold text-xl text-gray-800">{camping.name}</h3>
                                                <p className="text-sm text-gray-500 mt-1">{camping.region} {camping.city}</p>
                                                <p className="text-sm text-gray-400 mt-1 truncate">{camping.address}</p>
                                            </div>
                                            <div className="flex items-center justify-between mt-4">
                                                <div className="flex items-center gap-1">
                                                    <span className="text-yellow-400">★</span>
                                                    <span className="text-sm font-medium text-gray-700">
                                                        {camping.averageRating > 0 ? camping.averageRating.toFixed(1) : "-"}
                                                    </span>
                                                    <span className="text-xs text-gray-400">({camping.reviewCount}개)</span>
                                                </div>
                                                <p className="text-[#5C7A5C] font-bold text-lg">
                                                    {camping.minPrice > 0
                                                        ? `${camping.minPrice.toLocaleString()}원~/박`
                                                        : "가격 정보 없음"}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </Link>
                            ))}
                        </div>

                        {totalPages > 1 && (
                            <div className="flex justify-center items-center gap-2 mt-8">
                                <button
                                    onClick={() => handleSearch(page - 1)}
                                    disabled={page === 0}
                                    className="px-3 py-1 rounded-lg text-sm text-gray-500 hover:bg-gray-100 disabled:opacity-30"
                                >
                                    이전
                                </button>
                                {Array.from({ length: totalPages }, (_, i) => i)
                                    .filter((i) => i >= page - 2 && i <= page + 2)
                                    .map((i) => (
                                        <button
                                            key={i}
                                            onClick={() => handleSearch(i)}
                                            className={`w-8 h-8 rounded-full text-sm transition ${page === i ? "bg-[#5C7A5C] text-white" : "text-gray-500 hover:bg-gray-100"
                                                }`}
                                        >
                                            {i + 1}
                                        </button>
                                    ))}
                                <button
                                    onClick={() => handleSearch(page + 1)}
                                    disabled={page === totalPages - 1}
                                    className="px-3 py-1 rounded-lg text-sm text-gray-500 hover:bg-gray-100 disabled:opacity-30"
                                >
                                    다음
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>

            {/* 필터 드로어 오버레이 */}
            {filterOpen && (
                <div
                    className="fixed inset-0 bg-black/40 z-40"
                    onClick={() => setFilterOpen(false)}
                />
            )}

            {/* 필터 드로어 */}
            <div className={`fixed top-0 right-0 h-full w-80 bg-white shadow-2xl z-50 transform transition-transform duration-300 ${filterOpen ? "translate-x-0" : "translate-x-full"
                }`}>
                <div className="h-full overflow-y-auto p-6">

                    {/* 드로어 헤더 */}
                    <div className="flex items-center justify-between mb-6">
                        <h2 className="text-lg font-bold text-gray-800">검색 필터</h2>
                        <button
                            onClick={() => setFilterOpen(false)}
                            className="text-gray-400 hover:text-gray-600 text-2xl"
                        >
                            ×
                        </button>
                    </div>

                    {/* 키워드 */}
                    <div className="mb-5">
                        <label className="text-sm font-medium text-gray-700 mb-1.5 block">검색어</label>
                        <input
                            type="text"
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            placeholder="캠핑장 이름 또는 도시"
                            className="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-[#5C7A5C]"
                        />
                    </div>

                    {/* 체크인 */}
                    <div className="mb-5">
                        <label className="text-sm font-medium text-gray-700 mb-1.5 block">체크인</label>
                        <input
                            type="date"
                            value={checkIn}
                            min={today}
                            onChange={(e) => setCheckIn(e.target.value)}
                            className="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-[#5C7A5C]"
                        />
                    </div>

                    {/* 체크아웃 */}
                    <div className="mb-5">
                        <label className="text-sm font-medium text-gray-700 mb-1.5 block">체크아웃</label>
                        <input
                            type="date"
                            value={checkOut}
                            min={checkIn}
                            onChange={(e) => setCheckOut(e.target.value)}
                            className="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-[#5C7A5C]"
                        />
                    </div>

                    {/* 숙박 인원 */}
                    <div className="mb-5">
                        <label className="text-sm font-medium text-gray-700 mb-1.5 block">1팀 인원</label>
                        <div className="flex items-center gap-3 border border-gray-200 rounded-xl px-4 py-2.5">
                            <button onClick={() => setGuestCount(Math.max(1, guestCount - 1))}
                                className="text-gray-500 hover:text-[#5C7A5C] font-bold text-lg">−</button>
                            <span className="flex-1 text-center text-sm">{guestCount}명</span>
                            <button onClick={() => setGuestCount(guestCount + 1)}
                                className="text-gray-500 hover:text-[#5C7A5C] font-bold text-lg">+</button>
                        </div>
                    </div>

                    {/* 객실 수 */}
                    <div className="mb-5">
                        <label className="text-sm font-medium text-gray-700 mb-1.5 block">예약할 사이트 수</label>
                        <div className="flex items-center gap-3 border border-gray-200 rounded-xl px-4 py-2.5">
                            <button onClick={() => setRoomCount(Math.max(1, roomCount - 1))}
                                className="text-gray-500 hover:text-[#5C7A5C] font-bold text-lg">−</button>
                            <span className="flex-1 text-center text-sm">{roomCount}개</span>
                            <button onClick={() => setRoomCount(roomCount + 1)}
                                className="text-gray-500 hover:text-[#5C7A5C] font-bold text-lg">+</button>
                        </div>
                    </div>

                    {/* 가격 범위 듀얼 슬라이더 */}
                    <div className="mb-6">
                        <label className="text-sm font-medium text-gray-700 mb-1.5 block">
                            가격 범위 (1박 기준)
                        </label>
                        <div className="flex justify-between text-xs text-gray-500 mb-3">
                            <span>{minPrice.toLocaleString()}원</span>
                            <span>{maxPrice >= MAX_PRICE ? "제한 없음" : `${maxPrice.toLocaleString()}원`}</span>
                        </div>

                        {/* 듀얼 슬라이더 */}
                        <div className="relative h-6">
                            {/* 트랙 */}
                            <div className="absolute top-1/2 -translate-y-1/2 w-full h-1.5 bg-gray-200 rounded-full" />
                            {/* 선택 범위 */}
                            <div
                                className="absolute top-1/2 -translate-y-1/2 h-1.5 bg-[#5C7A5C] rounded-full"
                                style={{ left: `${minPercent}%`, width: `${maxPercent - minPercent}%` }}
                            />
                            {/* min 핸들 */}
                            <input
                                type="range"
                                min={0}
                                max={MAX_PRICE}
                                step={STEP}
                                value={minPrice}
                                onChange={(e) => {
                                    const val = Number(e.target.value);
                                    if (val < maxPrice - STEP) setMinPrice(val);
                                }}
                                className="absolute w-full h-full opacity-0 cursor-pointer"
                                style={{ zIndex: minPrice > MAX_PRICE - STEP * 2 ? 5 : 3 }}
                            />
                            {/* max 핸들 */}
                            <input
                                type="range"
                                min={0}
                                max={MAX_PRICE}
                                step={STEP}
                                value={maxPrice}
                                onChange={(e) => {
                                    const val = Number(e.target.value);
                                    if (val > minPrice + STEP) setMaxPrice(val);
                                }}
                                className="absolute w-full h-full opacity-0 cursor-pointer"
                                style={{ zIndex: 4 }}
                            />
                            {/* 핸들 표시 */}
                            <div
                                className="absolute top-1/2 -translate-y-1/2 w-4 h-4 bg-white border-2 border-[#5C7A5C] rounded-full pointer-events-none"
                                style={{ left: `calc(${minPercent}% - 8px)` }}
                            />
                            <div
                                className="absolute top-1/2 -translate-y-1/2 w-4 h-4 bg-white border-2 border-[#5C7A5C] rounded-full pointer-events-none"
                                style={{ left: `calc(${maxPercent}% - 8px)` }}
                            />
                        </div>
                    </div>

                    <button
                        onClick={() => handleSearch(0)}
                        className="w-full py-3 bg-[#5C7A5C] text-white rounded-xl font-semibold hover:bg-[#4a6349] transition"
                    >
                        검색
                    </button>
                </div>
            </div>
        </div>
    );
}