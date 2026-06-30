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

    const getWelcomeMessage = () => {
        if (role === "ADMIN") return "관리자님";
        if (role === "HOST") return "호스트님";
        if (role === "USER") return "회원님";
        return "";
    };

    return (
        <header>
            <div className="max-w-6xl mx-auto px-6">
                <div className="flex items-center justify-between py-4 border-b">

                    {/* LEFT - logo + welcome */}
                    <div className="flex items-center gap-4">
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

                        {mounted && isLoggedIn && (
                            <span className="text-sm text-gray-500">
                                어서오세요, <b>{getWelcomeMessage()}</b>
                            </span>
                        )}
                    </div>

                    {/* NAV */}
                    <nav className="flex items-center gap-8 text-gray-600 font-medium">

                        <Link href="/campings/search" className="hover:text-[#4B6945]">
                            캠핑장 검색
                        </Link>

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

                                {role === "USER" && (
                                    <Link href="/mypage" className="hover:text-[#4B6945]">
                                        마이페이지
                                    </Link>
                                )}

                                <NotificationBell />

                                <button
                                    onClick={handleLogout}
                                    className="hover:text-[#4B6945]"
                                >
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