import {
    ApiResponse,
    PageResponse,
    HostProfileResponse,
    CampingCreateRequest,
    CampingCreateResponse,
    CampingUpdateRequest,
    CampingUpdateResponse,
    HostCampingListItem,
    HostCampingDetail,
    Site,
    SiteCreateRequest,
    SiteCreateResponse,
    SiteUpdateRequest,
    SiteUpdateResponse,
    CampingImageResponse,
    HostReservationListItem,
    CampingClaimRequest,
    CampingClaimSearchItem,
  } from "@/types/host";
  
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;
  
  async function request<T>(
    url: string,
    options: RequestInit = {}
  ): Promise<T> {
    const isFormData = options.body instanceof FormData;
  
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers: {
        ...(isFormData ? {} : { "Content-Type": "application/json" }),
        ...options.headers,
      },
      credentials: "include",
    });
  
    if (!response.ok) {
      const error = await response.json();
    
      throw new Error(
        error.data ?? error.message ?? "API 요청에 실패했습니다."
      );
    }
  
    return response.json();
  }


  // 호스트 프로필 조회
  export async function getHostProfile() {
    const response = await request<ApiResponse<HostProfileResponse>>(
      "/api/users/me"
    );
  
    return response.data;
  }
  
  // 내 캠핑장 목록 조회
  export async function getMyCampings() {
    const response = await request<ApiResponse<HostCampingListItem[]>>(
      "/api/host/campings"
    );
  
    return response.data;
  }
  
  // 내 캠핑장 상세 조회
  export async function getHostCampingDetail(campingId: number) {
    const response = await request<ApiResponse<HostCampingDetail>>(
      `/api/host/campings/${campingId}`
    );
  
    return response.data;
  }
  
  // 캠핑장 등록
  export async function createCamping(data: CampingCreateRequest) {
    const response = await request<ApiResponse<CampingCreateResponse>>(
      "/api/host/campings",
      {
        method: "POST",
        body: JSON.stringify(data),
      }
    );
  
    return response.data;
  }
  
  // 캠핑장 수정
  export async function updateCamping(
    campingId: number,
    data: CampingUpdateRequest
  ) {
    const response = await request<ApiResponse<CampingUpdateResponse>>(
      `/api/host/campings/${campingId}`,
      {
        method: "PATCH",
        body: JSON.stringify(data),
      }
    );
  
    return response.data;
  }
  
  // 캠핑장 삭제
  export async function deleteCamping(campingId: number) {
    await request<ApiResponse<null>>(`/api/host/campings/${campingId}`, {
      method: "DELETE",
    });
  }

  // 구역 목록 조회
  export async function getHostSites(campingId: number) {
    const response = await request<ApiResponse<Site[]>>(
      `/api/host/campings/${campingId}/sites`
    );
  
    return response.data;
  }
  
  // 구역 등록
  export async function createSite(
    campingId: number,
    data: SiteCreateRequest
  ) {
    const response = await request<ApiResponse<SiteCreateResponse>>(
      `/api/host/campings/${campingId}/sites`,
      {
        method: "POST",
        body: JSON.stringify(data),
      }
    );
  
    return response.data;
  }
  
  // 구역 수정
  export async function updateSite(
    campingId: number,
    siteId: number,
    data: SiteUpdateRequest
  ) {
    const response = await request<ApiResponse<SiteUpdateResponse>>(
      `/api/host/campings/${campingId}/sites/${siteId}`,
      {
        method: "PATCH",
        body: JSON.stringify(data),
      }
    );
  
    return response.data;
  }
  
  // 구역 삭제
  export async function deleteSite(campingId: number, siteId: number) {
    await request<ApiResponse<null>>(
      `/api/host/campings/${campingId}/sites/${siteId}`,
      {
        method: "DELETE",
      }
    );
  }

  // 캠핑장 이미지 등록
  export async function addCampingImage(
    campingId: number,
    image: File,
    thumbnail: boolean
  ) {
    const formData = new FormData();
    formData.append("image", image);
  
    const response = await request<ApiResponse<CampingImageResponse>>(
      `/api/host/campings/${campingId}/images?thumbnail=${thumbnail}`,
      {
        method: "POST",
        body: formData,
      }
    );
  
    return response.data;
  }
  
  // 캠핑장 이미지 삭제
  export async function deleteCampingImage(
    campingId: number,
    imageId: number
  ) {
    await request<ApiResponse<null>>(
      `/api/host/campings/${campingId}/images/${imageId}`,
      {
        method: "DELETE",
      }
    );
  }

  // 예약 목록 조회
  export async function getHostReservations() {
    const response = await request<ApiResponse<PageResponse<HostReservationListItem>>>(
      "/api/host/reservations"
    );
  
    return response.data.content;
  }
 
  // 클레임 가능한 캠핑장 검색
  export async function searchClaimableCampings(keyword: string) {
    const response = await request<ApiResponse<CampingClaimSearchItem[]>>(
      `/api/host/campings/claim/search?keyword=${encodeURIComponent(keyword)}`
    );

    return response.data;
  }

  // 캠핑장 소유권 인증
  export async function claimCamping(data: CampingClaimRequest) {
    await request<ApiResponse<null>>("/api/host/campings/claim", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }