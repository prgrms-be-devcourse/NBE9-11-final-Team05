"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import HostProfile from "@/components/host/HostProfile";
import { getHostProfile } from "@/lib/api/host";
import { HostProfileResponse } from "@/types/host";

export default function HostDashboardPage() {
  const router = useRouter();
  const [isAuthorized, setIsAuthorized] = useState(false);

  useEffect(() => {
    async function checkRole() {
      try {
        const profile: HostProfileResponse = await getHostProfile();

        if (profile.role !== "HOST") {
          router.replace("/");
          return;
        }

        setIsAuthorized(true);
      } catch (error) {
        console.error(error);
        router.replace("/auth/login");
      }
    }

    checkRole();
  }, [router]);

  if (!isAuthorized) {
    return null;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">
        호스트 대시보드
      </h1>

      <HostProfile />
    </div>
  );
}