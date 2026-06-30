import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Header from "@/components/layout/Header";
import "./globals.css";
import ChatWindow from "@/components/chat/ChatWindow";
import ChatFabButton from "@/components/chat/ChatFabButton";
import { Toaster } from "sonner";
import WebSocketProvider from "@/components/ws/WebSocketProvider";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "캠핑가잣",
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
        <WebSocketProvider>
          
          {/* Header */}
          <Header />

          {/* Main */}
          <main className="max-w-6xl mx-auto px-6 py-8 min-h-screen">
            {children}
          </main>

          <ChatFabButton />
          <ChatWindow />
          
          <Toaster
            position="top-center"
            toastOptions={{
              duration: 1800,
            }}
          />

          {/* Footer */}
          <footer>
            <div className="max-w-6xl mx-auto px-6">
              <div className="border-t py-8 text-sm text-gray-500">
                <div className="font-semibold text-gray-700 mb-2">
                  캠핑가잣
                </div>
                <p>전국 캠핑장 예약 플랫폼</p>
                <p className="mt-4">
                  © 2026 캠핑가잣. All rights reserved.
                </p>
              </div>
            </div>
          </footer>

        </WebSocketProvider>
      </body>
    </html>
  );
}