"use client";

import { useEffect, useState } from "react";
import { getHostProfile } from "@/lib/api/host";
import { HostProfileResponse } from "@/types/host";

export default function HostProfile() {
  const [profile, setProfile] = useState<HostProfileResponse | null>(null);

  useEffect(() => {
    async function fetchProfile() {
      try {
        const data = await getHostProfile();
        setProfile(data);
      } catch (error) {
        console.error(error);
      }
    }

    fetchProfile();
  }, []);

  if (!profile) {
    return <p>프로필 정보를 불러오는 중...</p>;
  }

  return (
    <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-gray-900">
        호스트 정보
      </h2>
  
      <div className="flex items-center gap-4">
        {profile.imageUrl ? (
          <img
            src={profile.imageUrl}
            alt={profile.nickname}
            width={80}
            height={80}
            className="h-20 w-20 rounded-full object-cover"
          />
        ) : (
          <div className="flex h-20 w-20 items-center justify-center rounded-full bg-gray-100 text-sm text-gray-400">
            이미지
          </div>
        )}
  
        <div className="space-y-1">
          <p className="font-medium text-gray-900">
            {profile.nickname}
          </p>
  
          <p className="text-sm text-gray-600">
            {profile.phone}
          </p>
        </div>
      </div>
    </section>
  );
}