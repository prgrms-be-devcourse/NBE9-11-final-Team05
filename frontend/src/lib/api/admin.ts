import { AdminDashboardResponse, AdminPendingCampingResponse } from "@/types/admin";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export async function getDashboard(): Promise<AdminDashboardResponse> {
  const res = await fetch(`${API_URL}/api/admin/dashboard`, {
    credentials: "include",
  });

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message);
  }

  return result.data;
}

/* 승인 대기 캠핑장 목록 */
export async function getPendingCampings(
  page = 0
): Promise<AdminPendingCampingResponse> {

  const res = await fetch(
    `${API_URL}/api/admin/campings?page=${page}`,
    {
      credentials: "include",
      cache: "no-store",
    }
  );

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message);
  }

  return result.data;
}

/* 캠핑장 승인 */
export async function approveCamping(campingId: number) {
  const res = await fetch(
    `${API_URL}/api/admin/campings/${campingId}/approve`,
    {
      method: "PATCH",
      credentials: "include",
    }
  );

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message);
  }
}

/* 캠핑장 반려 */
export async function rejectCamping(
  campingId: number,
  rejectReason: string
) {
  const res = await fetch(
    `${API_URL}/api/admin/campings/${campingId}/reject`,
    {
      method: "PATCH",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        rejectReason,
      }),
    }
  );

  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.message);
  }
}

/* 일괄 승인 */
export const approveCampingBulk = async (campingIds: number[]) => {
  const res = await fetch("/api/admin/campings/approve", {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      campingIds,
    }),
  });

  if (!res.ok) {
    throw new Error("일괄 승인 실패");
  }

  return res.json();
};