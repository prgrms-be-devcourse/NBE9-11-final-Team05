"use client";

import { useEffect, useState } from "react";
import { useAuthStore } from "@/stores/authStore";
import { notificationApi, NotificationItem } from "@/lib/api/notification";
import { Bell } from "lucide-react";
import { API_BASE_URL, ApiError } from "@/lib/api/core";;


export default function NotificationBell() {
    const { isLoggedIn, clearAuth } = useAuthStore();
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState<NotificationItem[]>([]);
    const [isOpen, setIsOpen] = useState(false);

    // 안 읽은 알림 개수 조회
    const fetchUnreadCount = async () => {
        try {
            const res = await notificationApi.getUnreadCount();
            setUnreadCount(res.data.unreadCount);
        } catch (e) {
            console.error(e);
            if (e instanceof ApiError && e.status === 401) {
                clearAuth();
            }
        }
    };

    // 알림 목록 조회
    const fetchNotifications = async () => {
        try {
            const res = await notificationApi.getNotifications();
            setNotifications(res.data.content);
        } catch (e) {
            console.error(e);
            if (e instanceof ApiError && e.status === 401) {
                clearAuth();
            }
        }
    };

    // 알림 읽음 처리
    const readNotification = async (notificationId: number) => {
        try {
            await notificationApi.readNotification(notificationId);
            setNotifications((prev) =>
                prev.map((n) =>
                    n.notificationId === notificationId ? { ...n, isRead: true } : n
                )
            );
            setUnreadCount((prev) => Math.max(0, prev - 1));
        } catch (e) {
            console.error(e);
            if (e instanceof ApiError && e.status === 401) {
                clearAuth();
            }
        }
    };


    // SSE 구독
    useEffect(() => {
        if (!isLoggedIn) return;

        fetchUnreadCount();

        const eventSource = new EventSource(
            `${API_BASE_URL}/api/notifications/subscribe`,
            { withCredentials: true }
        );

        eventSource.addEventListener("notification", (e) => {
            try {
                const notification = JSON.parse(e.data);
                setNotifications((prev) => [notification, ...prev]);
                setUnreadCount((prev) => prev + 1);
            } catch (err) {
                console.error("Failed to parse notification data:", err);
            }
        });

        eventSource.onerror = (err) => {
            console.error("SSE connection error:", err);
            // clearAuth() 제거 - 일시적 오류에도 로그아웃되면 안 됨
        };

        return () => {
            eventSource.close();
        };
    }, [isLoggedIn]);

    if (!isLoggedIn) return null;

    return (
        <div className="relative">
            {/* 알림 벨 버튼 */}
            <button
                onClick={() => {
                    setIsOpen((prev) => !prev);
                    if (!isOpen) fetchNotifications();
                }}
                className="relative p-2 hover:text-[#4B6945]"
            >
                <Bell size={20} />
                {unreadCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {unreadCount > 9 ? "9+" : unreadCount}
                    </span>
                )}
            </button>

            {/* 알림 드롭다운 */}
            {isOpen && (
                <div className="absolute right-0 mt-2 w-80 bg-white border rounded-xl shadow-lg z-50">
                    <div className="px-4 py-3 border-b font-semibold text-gray-700">
                        알림
                    </div>
                    <ul className="max-h-80 overflow-y-auto">
                        {notifications.length === 0 ? (
                            <li className="px-4 py-6 text-center text-gray-400 text-sm">
                                알림이 없습니다.
                            </li>
                        ) : (
                            notifications.map((n) => (
                                <li
                                    key={n.notificationId}
                                    onClick={() => !n.isRead && readNotification(n.notificationId)}
                                    className={`px-4 py-3 text-sm border-b cursor-pointer hover:bg-gray-50 ${n.isRead ? "text-gray-400" : "text-gray-700 font-medium"
                                        }`}
                                >
                                    <p>{n.content}</p>
                                    <p className="text-xs text-gray-400 mt-1">
                                        {new Date(n.createdAt).toLocaleString("ko-KR")}
                                    </p>
                                </li>
                            ))
                        )}
                    </ul>
                </div>
            )}
        </div>
    );
}