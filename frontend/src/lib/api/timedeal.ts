// lib/api/timedeal.ts

export interface TimeDealResponse {
    id: number;
    siteId: number;
    siteName: string;
    campingId: number;
    campingName: string;
    checkIn: string;
    checkOut: string;
    quantity: number;
    soldCount: number;
    remaining: number;
    originalPrice: number;
    dealPrice: number;
    discountRate: number;
    saleStartAt: string;
    saleEndAt: string;
    status: "SCHEDULED" | "ACTIVE" | "SOLD_OUT" | "ENDED" | "CANCELLED";
  }
  
  export async function getActiveTimeDeals(): Promise<TimeDealResponse[]> {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/api/timedeals?size=6`,
      { cache: "no-store" }
    );
    if (!res.ok) return [];
    const json = await res.json();
    return json.data.content ?? [];
  }
  
  export async function getTimeDeal(id: number): Promise<TimeDealResponse> {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/api/timedeals/${id}`,
      { cache: "no-store" }
    );
    if (!res.ok) throw new Error("타임딜을 찾을 수 없습니다.");
    const json = await res.json();
    return json.data;
  }
  
  export interface TimeDealReservationRequest {
    rsvName: string;
    rsvPhone: string;
    guestCount: number;
    request?: string;
  }
  
  export async function createTimeDealReservation(
    timeDealId: number,
    body: TimeDealReservationRequest
  ): Promise<{ id: number }> {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/api/reservations/timedeal/${timeDealId}`,
      {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      }
    );
    const json = await res.json();
    if (!res.ok) throw new Error(json.message ?? "예약에 실패했습니다.");
    return json.data;
  }