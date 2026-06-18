import { fetchApi } from "./api";
import { Camping, CampingDetail } from "../types/camping";

export async function getLatestCampings(): Promise<Camping[]> {
  const response = await fetchApi<{
    content: Camping[];
  }>(
    "/api/campings?page=0&size=3&sort=createdAt,asc"
  );

  return response.content;
}

export async function getCampingDetail(
  campingId: number
): Promise<CampingDetail> {
  return fetchApi<CampingDetail>(
    `/api/campings/${campingId}`
  );
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

  return fetchApi(
    `/api/campings?${params.toString()}`
  );
}