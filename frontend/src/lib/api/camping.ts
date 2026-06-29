import { Camping, CampingDetail, CampingSearchResponse } from "@/types/camping";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export async function getLatestCampings(): Promise<Camping[]> {
  const response = await fetch(
    `${API_URL}/api/campings?page=0&size=3&sort=createdAt,desc`,
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
  size: number = 12
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

{/* 예약 가능 캠핑장 검색 */ }
export async function searchCampings(params: {
  keyword?: string;
  checkIn?: string;
  checkOut?: string;
  guestCount?: number;
  roomCount?: number;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
}): Promise<{
  content: CampingSearchResponse[];
  hasNext: boolean;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}> {
  const query = new URLSearchParams();
  if (params.keyword) query.append("keyword", params.keyword);
  if (params.checkIn) query.append("checkIn", params.checkIn);
  if (params.checkOut) query.append("checkOut", params.checkOut);
  if (params.guestCount) query.append("guestCount", String(params.guestCount));
  if (params.roomCount) query.append("roomCount", String(params.roomCount));
  if (params.minPrice) query.append("minPrice", String(params.minPrice));
  if (params.maxPrice) query.append("maxPrice", String(params.maxPrice));
  if (params.page !== undefined) query.append("page", String(params.page));
  if (params.size !== undefined) query.append("size", String(params.size));

  const response = await fetch(`${API_URL}/api/campings/search?${query}`, {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`API 호출 실패: ${response.status}`);
  }

  const result = await response.json();
  return result.data;
}