import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Image from "next/image";
import Link from "next/link";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "캠핑가자",
  description: "캠핑장 예약 플랫폼",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body className={`${geistSans.variable} ${geistMono.variable}`}>
        {/* Header */}
        <header>
          <div className="max-w-6xl mx-auto px-6">
            <div className="flex items-center justify-between py-4 border-b">
              <Link href="/" className="flex items-center gap-3">
                <Image
                  src="/images/camping-logo.png"
                  alt="캠핑가자 로고"
                  width={95}
                  height={95}
                />
              </Link>

              <nav className="flex items-center gap-8 text-gray-600 font-medium">
                <Link href="/host" className="hover:text-[#4B6945]">
                  호스트 페이지
                </Link>

                <Link href="/auth/login" className="hover:text-[#4B6945]">
                  로그인
                </Link>

                <Link href="auth/signup" className="hover:text-[#4B6945]">
                  회원가입
                </Link>
              </nav>
            </div>
          </div>
        </header>

        {/* Main */}
        <main className="max-w-6xl mx-auto px-6 py-8 min-h-screen">
          {children}
        </main>

        {/* Footer */}
        <footer>
          <div className="max-w-6xl mx-auto px-6">
            <div className="border-t py-8 text-sm text-gray-500">
              <div className="font-semibold text-gray-700 mb-2">
                캠핑가자
              </div>

              <p>전국 캠핑장 예약 플랫폼</p>

              <p className="mt-4">
                © 2026 캠핑가자. All rights reserved.
              </p>
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}