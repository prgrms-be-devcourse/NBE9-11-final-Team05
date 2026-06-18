import Link from "next/link";
import HostProfile from "@/components/host/HostProfile";

export default function HostDashboardPage() {
  return (
    <main>
      <h1>호스트 대시보드</h1>

      <HostProfile />

      <section>
        <h2>빠른 메뉴</h2>

        <Link href="/host/campings">내 캠핑장 관리</Link>
        <Link href="/host/campings/new">캠핑장 등록</Link>
      </section>
    </main>
  );
}