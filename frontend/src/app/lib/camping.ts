import { Camping } from "../types/camping";

export async function getLatestCampings(): Promise<Camping[]> {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/api/campings?page=0&size=3&sort=createdAt,desc`
  );

  if (!res.ok) {
    throw new Error("캠핑장 목록 조회 실패");
  }

  const data = await res.json();
  
  console.log(data);

  return data.data?.content ?? [];
}