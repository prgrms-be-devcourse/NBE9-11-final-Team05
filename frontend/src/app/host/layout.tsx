import Link from "next/link";

export default function HostLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div>
      <nav>
        <Link href="/host/dashboard">대시보드</Link>
        <Link href="/host/campings">내 캠핑장</Link>
        <Link href="/host/campings/new">캠핑장 등록</Link>
      </nav>

      {children}
    </div>
  );
}