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