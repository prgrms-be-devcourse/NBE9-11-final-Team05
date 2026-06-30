// 예약 생성 요청
export interface CreateReservationRequest {
    siteId: number;
    rsvName: string;
    rsvPhone: string;
    checkIn: string; // "YYYY-MM-DD"
    checkOut: string; // "YYYY-MM-DD"
    guestCount: number;
    request?: string;
  }
  
  // 예약 상태값
  // 백엔드 enum과 명칭이 다를 수 있으니, 실제 값 확인 후 보정 필요
  export type ReservationStatus =
    | "PENDING" // 결제 대기
    | "CONFIRMED" // 예약 확정 (결제 완료)
    | "CANCELLED"; // 취소
  
  // 예약 응답 (예약생성/상세조회 공통으로 추정, 실제 응답 확인 후 보정 필요)
  export interface ReservationResponse {
    id: number;
    siteId: number;
    siteName?: string;
    campingName?: string;
    rsvName: string;
    rsvPhone: string;
    checkIn: string;
    checkOut: string;
    guestCount: number;
    request?: string;
    status: ReservationStatus;
    totalPrice: number;
    createdAt?: string;
  }
  
  // 예약 생성 응답 (백엔드 실제 응답 기준)
export interface ReservationCreateResponse {
  checkIn: string;
  checkOut: string;
  createdAt: string;
  guestCount: number;
  id: number;
  imageUrl: string | null;
  rsvNum: string;
  rsvPrice: number;
  siteId: number;
  status: string;
  userId: number;
}

// 예약 상세조회 응답 (백엔드 실제 응답 기준)
export interface ReservationDetailResponse {
  campingId: number;
  address: string;
  campingName: string;
  checkIn: string;
  checkOut: string;
  createdAt: string;
  guestCount: number;
  id: number;
  imageUrl: string | null;
  request: string | null;
  rsvName: string;
  rsvNum: string;
  rsvPhone: string;
  rsvPrice: number;
  siteName: string;
  status: string;
}

// 예약 목록 아이템 (실제 응답 확인 후 보정 필요)
export interface ReservationListItem {
  id: number;
  campingName: string;
  siteName: string;
  checkIn: string;
  checkOut: string;
  status: string;
  rsvPrice: number;
  imageUrl: string | null;
}

// 내 예약 목록조회 (Spring Page 형태)
export interface ReservationListResponse {
  content: ReservationListItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}