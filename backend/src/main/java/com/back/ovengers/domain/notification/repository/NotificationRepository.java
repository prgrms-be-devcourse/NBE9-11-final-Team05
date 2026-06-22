package com.back.ovengers.domain.notification.repository;

import com.back.ovengers.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알람 목록 조회
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 안읽은 알람 개수
    long countByUserIdAndIsReadFalse(Long userId);

    // 전체 읽음 처리(벌크 처리)
    // @Modifying 사용 시 db에 직접 반영 clearAutomatically로 해결 가능하지만 1차 캐시를 비워버림
    // 만약 추후 응답에 최신 알림 목록/unreadCount를 함께 반환하도록 API 스펙이 변경될 경우
    // 1차 캐시 불일치 방지를 위해 clearAutomatically = true 추가 필요
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
}
