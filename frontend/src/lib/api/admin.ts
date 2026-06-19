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