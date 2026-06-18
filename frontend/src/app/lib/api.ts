const BASE_URL = process.env.NEXT_PUBLIC_API_URL;

export async function fetchApi<T>(
  url: string
): Promise<T> {

  const response = await fetch(
    `${BASE_URL}${url}`,
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