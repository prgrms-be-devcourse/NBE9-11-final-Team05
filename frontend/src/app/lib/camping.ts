import { Camping } from "../types/camping";

export async function getPopularCampings(): Promise<Camping[]> {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/api/campings?page=0&size=3`
  );

  const data = await res.json();
  
  return data.data.content;
}