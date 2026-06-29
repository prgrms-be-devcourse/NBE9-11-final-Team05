export interface AdminDashboardResponse {
  totalSalesAmount: number;
  activeUserCount: number;
  pendingCampingCount: number;
}

export interface PendingCampingResponse {
  campingId: number;
  campingName: string;
  hostName: string | null;
  createdAt: string;
  businessNum: string;
}

export interface AdminPendingCampingResponse {
  pendingCampingCount: number;
  content: PendingCampingResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface AdminUserResponse {
  userId: number;
  email: string;
  name: string;
  nickname: string;
  role: string;
  status: string;
  isDeleted: boolean;
  createdAt: string;
}

export interface AdminUserListResponse {
  content: AdminUserResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}