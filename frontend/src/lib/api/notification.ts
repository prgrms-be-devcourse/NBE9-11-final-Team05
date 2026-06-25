import { apiFetch } from "./core";

export interface NotificationItem {
    notificationId: number;
    type: string;
    content: string;
    isRead: boolean;
    createdAt: string;
}

interface NotificationListResponse {
    content: NotificationItem[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
}

interface UnreadCountResponse {
    unreadCount: number;
}

export const notificationApi = {
    // 알림 목록 조회
    getNotifications: (page = 0, size = 10) =>
        apiFetch<{ message: string; data: NotificationListResponse }>(
            `/api/notifications?page=${page}&size=${size}`
        ),

    // 안 읽은 알림 개수 조회
    getUnreadCount: () =>
        apiFetch<{ message: string; data: UnreadCountResponse }>(
            `/api/notifications/unread-count`
        ),

    // 알림 단건 읽음 처리
    readNotification: (notificationId: number) =>
        apiFetch<{ message: string; data: null }>(
            `/api/notifications/${notificationId}/read`,
            { method: "PATCH" }
        ),

    // 알림 전체 읽음 처리
    readAll: () =>
        apiFetch<{ message: string; data: null }>(
            `/api/notifications/read-all`,
            { method: "PATCH" }
        ),
};