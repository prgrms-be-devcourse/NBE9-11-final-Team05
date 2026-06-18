import Link from "next/link";
import HostProfile from "@/components/host/HostProfile";

export default function HostDashboardPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">호스트 대시보드</h1>

      <HostProfile />

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">빠른 메뉴</h2>

        <div className="flex gap-3">
          <Link
            href="/host/campings"
            className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            내 캠핑장 관리
          </Link>

          <Link
            href="/host/campings/new"
            className="rounded-md bg-gray-900 px-4 py-2 text-sm text-white hover:bg-gray-700"
          >
            캠핑장 등록
          </Link>
        </div>
      </section>
    </div>
  );
}