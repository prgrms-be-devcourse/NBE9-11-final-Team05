"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { useAuthStore } from "@/stores/authStore";
import NotificationBell from "@/components/notification/NotificationBell";
import { apiFetch } from "@/lib/api/core";

export default function Header() {
    const { isLoggedIn, role, clearAuth } = useAuthStore();
    const [mounted, setMounted] = useState(false);

    // hydration 완료 후에만 렌더링
    useEffect(() => {
        setMounted(true);
    }, []);

    const handleLogout = async () => {
        try {
            await apiFetch("/api/auth/logout", { method: "POST" });
        } catch (e) {
            console.error(e);
        } finally {
            clearAuth();
            window.location.href = "/";
        }
    };

    return (
        <header>
            <div className="max-w-6xl mx-auto px-6">
                <div className="flex items-center justify-between py-4 border-b">
                    <Link href="/" className="flex items-center gap-3">
                        <Image
                            src="/images/camping-logo.png"
                            alt="캠핑가잣 로고"
                            width={120}
                            height={95}
                            className="h-[40px] w-auto"
                            priority
                        />
                    </Link>

                    <nav className="flex items-center gap-8 text-gray-600 font-medium">

                        {/* 캠핑장 검색 탭 추가 */}
                        <Link href="/campings/search" className="hover:text-[#4B6945]">
                            캠핑장 검색
                        </Link>

                        {/* mounted 전에는 기본 링크만 표시 */}
                        {!mounted ? (
                            <>
                                <Link href="/auth/login" className="hover:text-[#4B6945]">
                                    로그인
                                </Link>
                                <Link href="/auth/signup/step1" className="hover:text-[#4B6945]">
                                    회원가입
                                </Link>
                            </>
                        ) : isLoggedIn ? (
                            <>
                                {role === "HOST" && (
                                    <Link href="/host/dashboard" className="hover:text-[#4B6945]">
                                        호스트 페이지
                                    </Link>
                                )}
                                {role === "ADMIN" && (
                                    <Link href="/admin/dashboard" className="hover:text-[#4B6945]">
                                        관리자 페이지
                                    </Link>
                                )}
                                <NotificationBell />
                                <Link href="/mypage" className="hover:text-[#4B6945]">
                                    마이페이지
                                </Link>
                                <button onClick={handleLogout} className="hover:text-[#4B6945]">
                                    로그아웃
                                </button>
                            </>
                        ) : (
                            <>
                                <Link href="/auth/login" className="hover:text-[#4B6945]">
                                    로그인
                                </Link>
                                <Link href="/auth/signup/step1" className="hover:text-[#4B6945]">
                                    회원가입
                                </Link>
                            </>
                        )}
                    </nav>
                </div>
            </div>
        </header>
    );
}