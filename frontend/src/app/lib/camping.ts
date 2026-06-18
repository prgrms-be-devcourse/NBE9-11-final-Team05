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