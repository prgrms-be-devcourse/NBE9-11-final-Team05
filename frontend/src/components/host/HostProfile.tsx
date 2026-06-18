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
    <section>
      <h2>호스트 정보</h2>

      {profile.imageUrl && (
        <img
          src={profile.imageUrl}
          alt={profile.nickname}
          width={100}
        />
      )}

      <p>닉네임: {profile.nickname}</p>
      <p>전화번호: {profile.phone}</p>
    </section>
  );
}