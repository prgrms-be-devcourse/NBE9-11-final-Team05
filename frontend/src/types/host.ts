export type CampingStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  hasNext: boolean;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface HostProfileResponse {
  id: number;
  nickname: string;
  phone: string;
  imageUrl: string | null;
  role: "USER" | "HOST" | "ADMIN";
}

export interface HostCampingListItem {
  id: number;
  name: string;
  region: string;
  city: string;
  firstImageUrl: string | null;
  rating: number | null;
  status: CampingStatus;
}

export interface HostCampingDetail {
  id: number;
  firstImageUrl: string | null;
  tourNum: string | null;
  businessNum: string | null;
  status: CampingStatus;
  name: string;
  region: string;
  city: string;
  address: string;
  description: string | null;
  notice: string | null;
  phone: string | null;
  homepage: string | null;
  checkInTime: string | null;
  checkOutTime: string | null;
  rating: number | null;
  images: CampingImage[];
}

export interface CampingCreateRequest {
  tourNum?: string | null;
  businessNum?: string | null;
  name: string;
  region: string;
  city: string;
  address: string;

  sites: SiteCreateRequest[];
}

export interface CampingCreateResponse {
  id: number;
  name: string;
  status: CampingStatus;
}

export interface CampingUpdateRequest {
  firstImageUrl?: string | null;
  name?: string;
  homepage?: string | null;
  region?: string;
  city?: string;
  address?: string;
  description?: string | null;
  phone?: string | null;
  checkInTime?: string | null;
  checkOutTime?: string | null;
  notice?: string | null;
}

export interface CampingUpdateResponse {
  id: number;
  firstImageUrl: string | null;
  name: string;
  region: string;
  city: string;
  address: string;
  description: string | null;
  notice: string | null;
  phone: string | null;
  homepage: string | null;
  checkInTime: string | null;
  checkOutTime: string | null;
  rating: number | null;
}

export interface Site {
  id: number;
  name: string;
  description: string | null;
  baseCapacity: number;
  maxCapacity: number;
  price: number;
  totalAmount: number;
}

export interface SiteCreateRequest {
    name: string;
    description?: string | null;
    baseCapacity: number;
    maxCapacity: number;
    totalAmount: number;
    price: number;
  }

  export interface SiteCreateResponse {
    id: number;
    name: string;
    description: string | null;
    baseCapacity: number;
    maxCapacity: number;
    totalAmount: number;
    price: number;
  }

  export interface SiteUpdateRequest {
    name?: string;
    description?: string | null;
    baseCapacity?: number;
    maxCapacity?: number;
    totalAmount?: number;
    price?: number;
  }

  export interface SiteUpdateResponse {
    id: number;
    name: string;
    description: string | null;
    baseCapacity: number;
    maxCapacity: number;
    totalAmount: number;
    price: number;
  }

  export interface CampingImage {
    imageId: number;
    imageUrl: string;
    thumbnail: boolean;
  }
  
  export interface CampingImageResponse {
    imageId: number;
    imageUrl: string;
    thumbnail: boolean;
  }

  export type ReservationStatus =
  | "PENDING"
  | "CONFIRMED"
  | "CANCELLED"
  | "COMPLETED";

export interface HostReservationListItem {
  id: number;
  campingName: string;
  siteName: string;
  rsvNum: string;
  rsvName: string;
  rsvPhone: string;
  checkIn: string;
  checkOut: string;
  guestCount: number;
  rsvPrice: number;
  status: ReservationStatus;
  createdAt: string;
}

export type HostCampingFormMode = "create" | "edit";

export interface HostCampingFormValues {
  tourNum: string;
  businessNum: string;
  firstImageUrl: string;
  name: string;
  homepage: string;
  region: string;
  city: string;
  address: string;
  description: string;
  phone: string;
  checkInTime: string;
  checkOutTime: string;
  notice: string;
  
  sites: SiteFormValues[];
}

export interface SiteFormValues {
    name: string;
    description: string;
    baseCapacity: string;
    maxCapacity: string;
    totalAmount: string;
    price: string;
}

export interface CampingClaimRequest {
  campingId: number;
  tourNum: string;
}

export interface CampingClaimSearchItem {
  campingId: number;
  name: string;
  region: string;
  city: string;
  address: string;
  firstImageUrl: string | null;
}