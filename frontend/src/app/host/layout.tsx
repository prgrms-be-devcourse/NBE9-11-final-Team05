import Link from "next/link";

export default function HostLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen bg-gray-50">
      <aside className="w-64 border-r border-gray-200 bg-white p-6">
        <h1 className="mb-8 text-xl font-bold text-gray-900">Host</h1>

        <nav className="flex flex-col gap-3">
          <Link href="/host/dashboard" className="rounded-md px-3 py-2 text-sm text-gray-700 hover:bg-gray-100">
            대시보드
          </Link>
          <Link href="/host/campings" className="rounded-md px-3 py-2 text-sm text-gray-700 hover:bg-gray-100">
            내 캠핑장 목록
          </Link>
          <Link href="/host/campings/claim" className="rounded-md px-3 py-2 text-sm text-gray-700 hover:bg-gray-100">
            내 캠핑장 찾기
          </Link>
          <Link href="/host/campings/new" className="rounded-md px-3 py-2 text-sm text-gray-700 hover:bg-gray-100">
            캠핑장 등록
          </Link>
          <Link href="/host/reservations" className="rounded-md px-3 py-2 text-sm text-gray-700 hover:bg-gray-100">
            예약 관리
          </Link>
          <Link href="/host/timedeals" className="rounded-md px-3 py-2 text-sm text-gray-700 hover:bg-gray-100">
            타임딜 관리
          </Link>
        </nav>
      </aside>

      <main className="flex-1 p-8">{children}</main>
    </div>
  );
}