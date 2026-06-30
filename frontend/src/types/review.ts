// 리뷰 작성/수정 요청
export interface ReviewRequest {
  rating: number; // 1~5
  content: string;
}

// 캠핑장 리뷰 목록 아이템
export interface ReviewResponse {
  reviewId: number;
  writerNickname: string;
  writerImageUrl: string | null;
  rating: number;
  content: string;
  createdAt: string;
}

// 내 리뷰(예약별) 상세
export interface ReviewDetail {
  reviewId: number;
  rating: number;
  content: string;
}

// 내 리뷰 목록 아이템 (예약 + 리뷰 작성 여부)
export interface MyReviewResponse {
  reservationId: number;
  campingId: number;
  campingName: string;
  campingImageUrl: string | null;
  checkIn: string;
  checkOut: string;
  reservationNumber: string;
  hasReview: boolean;
  review: ReviewDetail | null;
}
