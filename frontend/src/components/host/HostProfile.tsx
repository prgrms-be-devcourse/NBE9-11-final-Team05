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
    return (
      <div className="rounded-3xl border border-gray-100 bg-white p-8 shadow-sm">
        <p className="text-sm text-gray-500">
          프로필 정보를 불러오는 중...
        </p>
      </div>
    );
  }

  return (
    <section className="rounded-3xl border border-gray-100 bg-white p-8 shadow-sm">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-gray-900">
            호스트 정보
          </h2>
          <p className="mt-1 text-sm text-gray-500">
            내 계정 정보를 확인할 수 있습니다.
          </p>
        </div>

        <span className="rounded-full bg-[#FFF4EA] px-3 py-1 text-xs font-semibold text-[#D17A2F]">
          HOST
        </span>
      </div>

      <div className="flex items-center gap-5">
        {profile.imageUrl ? (
          <img
            src={profile.imageUrl}
            alt={profile.nickname}
            width={88}
            height={88}
            className="h-22 w-22 rounded-full border-4 border-[#F4F5F1] object-cover"
          />
        ) : (
          <div className="flex h-22 w-22 items-center justify-center rounded-full bg-[#F4F5F1] text-sm font-medium text-gray-500">
            HOST
          </div>
        )}

        <div className="flex-1">
          <p className="text-xl font-semibold text-gray-900">
            {profile.nickname}
          </p>

          <p className="mt-1 text-sm text-gray-500">
            {profile.phone}
          </p>

          <div className="mt-4 flex gap-2">
            <span className="rounded-full bg-[#F4F5F1] px-3 py-1 text-xs font-medium text-[#3F6B3F]">
              캠핑장 운영
            </span>

            <span className="rounded-full bg-[#F4F5F1] px-3 py-1 text-xs font-medium text-gray-600">
              호스트 계정
            </span>
          </div>
        </div>
      </div>
    </section>
  );
}