"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Tent,
  Search,
  PlusCircle,
  CalendarDays,
  Tag,
} from "lucide-react";

const menus = [
  {
    href: "/host/dashboard",
    label: "대시보드",
    icon: LayoutDashboard,
    match: "exact",
  },
  {
    href: "/host/campings",
    label: "내 캠핑장 목록",
    icon: Tent,
    match: "camping-list",
  },
  {
    href: "/host/campings/claim",
    label: "내 캠핑장 찾기",
    icon: Search,
    match: "exact",
  },
  {
    href: "/host/campings/new",
    label: "캠핑장 등록",
    icon: PlusCircle,
    match: "exact",
  },
  {
    href: "/host/reservations",
    label: "예약 관리",
    icon: CalendarDays,
    match: "prefix",
  },
  {
    href: "/host/timedeals",
    label: "타임딜 관리",
    icon: Tag,
    match: "prefix",
  },
] as const;

function isActiveMenu(
  pathname: string,
  href: string,
  match: "exact" | "prefix" | "camping-list"
) {
  if (match === "exact") {
    return pathname === href;
  }

  if (match === "prefix") {
    return pathname === href || pathname.startsWith(`${href}/`);
  }

  if (match === "camping-list") {
    return (
      pathname === "/host/campings" ||
      /^\/host\/campings\/\d+/.test(pathname)
    );
  }

  return false;
}

export default function HostLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();

  return (
    <div className="flex min-h-screen bg-[#F8FAFC]">
      <aside className="sticky top-0 flex h-screen w-60 flex-col border-r border-gray-100 bg-white px-5 py-8">
        <div className="mb-8">
          <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-[#F3F7F1]">
            <Tent className="h-6 w-6 text-[#3F6F43]" />
          </div>

          <h2 className="text-2xl font-bold text-gray-900">Host</h2>
          <p className="mt-1 text-sm text-gray-500">호스트 관리</p>
        </div>

        <div className="mb-6 border-t border-gray-100" />

        <nav className="flex flex-col gap-2">
          {menus.map((menu) => {
            const Icon = menu.icon;
            const active = isActiveMenu(pathname, menu.href, menu.match);

            return (
              <Link
                key={menu.href}
                href={menu.href}
                className={`group flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200 ${active
                    ? "bg-[#F3F7F1] text-[#3F6F43]"
                    : "text-gray-600 hover:bg-[#F7F8F5] hover:text-gray-900"
                  }`}
              >
                <Icon
                  className={`h-5 w-5 transition-colors ${active
                      ? "text-[#3F6F43]"
                      : "text-gray-400 group-hover:text-gray-600"
                    }`}
                />

                <span>{menu.label}</span>
              </Link>
            );
          })}
        </nav>
      </aside>

      <main className="flex-1 px-6 py-8">{children}</main>
    </div>
  );
}