import { Camping, CampingDetail } from "@/types/camping";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export async function getLatestCampings(): Promise<Camping[]> {
  const response = await fetch(
    `${API_URL}/api/campings?page=0&size=3&sort=createdAt,asc`,
    {
      cache: "no-store",
    }
  );

  if (!response.ok) {
    throw new Error(`API 호출 실패 : ${response.status}`);
  }

  const result = await response.json();

  return result.data.content;
}

export async function getCampingDetail(
  campingId: number
): Promise<CampingDetail> {
  const response = await fetch(
    `${API_URL}/api/campings/${campingId}`,
    {
      cache: "no-store",
    }
  );

  if (!response.ok) {
    throw new Error(`API 호출 실패 : ${response.status}`);
  }

  const result = await response.json();

  return result.data;
}

export async function getCampings(
  keyword?: string,
  page: number = 0,
  size: number = 10
): Promise<{
  content: Camping[];
  hasNext: boolean;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}> {
  const params = new URLSearchParams();

  params.append("page", String(page));
  params.append("size", String(size));

  if (keyword) {
    params.append("keyword", keyword);
  }

  const response = await fetch(
    `${API_URL}/api/campings?${params.toString()}`,
    {
      cache: "no-store",
    }
  );

  if (!response.ok) {
    throw new Error(`API 호출 실패 : ${response.status}`);
  }

  const result = await response.json();

  return result.data;
}