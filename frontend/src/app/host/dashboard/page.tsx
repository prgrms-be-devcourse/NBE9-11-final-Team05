import Link from "next/link";
import HostProfile from "@/components/host/HostProfile";

export default function HostDashboardPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">호스트 대시보드</h1>

      <HostProfile />

    </div>
  );
}