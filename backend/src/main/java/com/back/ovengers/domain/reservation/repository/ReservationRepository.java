package com.back.ovengers.domain.reservation.repository;

import com.back.ovengers.domain.reservation.dto.HostReservationResponse;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.projection.ReservedSiteCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    // 해당 날짜에 예약된 수 조회
    @Query("""
    SELECT r FROM Reservation r
    WHERE r.site.id = :siteId
    AND r.status != :cancelledStatus
    AND r.checkIn < :checkOut
    AND r.checkOut > :checkIn
""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findOverlappingReservationsWithLock(
            @Param("siteId") Long siteId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("cancelledStatus") ReservationStatus cancelledStatus
    );

    // ReservationRepository 추가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(@Param("id") Long id);

    @Query(value = """
    SELECT r FROM Reservation r
    JOIN FETCH r.site s
    JOIN FETCH s.camping c
    WHERE r.user.id = :userId
    ORDER BY r.createdAt DESC
""",
            countQuery = """
    SELECT COUNT(r) FROM Reservation r
    WHERE r.user.id = :userId
""")
    Page<Reservation> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT
            new com.back.ovengers.domain.reservation.dto.HostReservationResponse(
                r.id, c.name, s.name, r.rsvNum, r.rsvName, r.rsvPhone, r.checkIn, r.checkOut, r.guestCount, r.rsvPrice, r.status, r.createdAt
            )
            FROM Reservation r
               JOIN r.site s
               JOIN s.camping c
               WHERE c.host.id = :hostId
                    ORDER BY r.createdAt DESC
    """)
    Page<HostReservationResponse> findHostReservations(Long hostId, Pageable pageable);

    // site, camping 한번에 조회(리뷰에서 사용)
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.site s " +
            "JOIN FETCH s.camping " +
            "WHERE r.id = :id")
    Optional<Reservation> findByIdWithSiteAndCamping(@Param("id") Long id);

    // 내 예약 기반 리뷰 목록 조회
    // COMPLETED 예약만 조회 (리뷰 작성 가능한 예약)
    // Site, Camping LEFT JOIN FETCH로 N+1 방지
    // 소프트 딜리트된 캠핑장/사이트도 예약 내역은 보여줌
    @Query(
            value = "SELECT r FROM Reservation r " +
                    "LEFT JOIN FETCH r.site s " +
                    "LEFT JOIN FETCH s.camping c " +
                    "WHERE r.user.id = :userId " +
                    "AND r.status = :status",

            // fetch join 제외하고 카운트만
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "WHERE r.user.id = :userId " +
                    "AND r.status = :status"
    )
    Page<Reservation> findCompletedByUserId(
            @Param("userId") Long userId,
            @Param("status") ReservationStatus status,
            Pageable pageable
    );

    // 해당 캠핑장에 특정 상태(status)의 예약이 하나라도 있는지 확인
    boolean existsBySiteCampingIdAndStatus(
            Long campingId,
            ReservationStatus status
    );

    boolean existsBySiteIdAndStatus(
            Long siteId,
            ReservationStatus status
    );

    @Query("SELECT r FROM Reservation r JOIN FETCH r.site s JOIN FETCH s.camping c WHERE r.user.id = :userId")
    List<Reservation> findAllByUserId(@Param("userId") Long userId);



    // 특정 Site에 대해 지정한 날짜와 겹치는 예약 건수를 조회
    @Query("""
    SELECT COUNT(r) FROM Reservation r
    WHERE r.site.id = :siteId
      AND r.status IN ('PENDING', 'CONFIRMED')
      AND r.checkIn < :checkOut AND r.checkOut > :checkIn
    """)
    long countActiveReservationsByOverlappingDates(@Param("siteId") Long siteId,
                                                   @Param("checkIn") LocalDate checkIn,
                                                   @Param("checkOut") LocalDate checkOut);

    @Query("""
    SELECT r FROM Reservation r
    LEFT JOIN FETCH r.timeDeal
    WHERE r.status = 'PENDING'
      AND r.createdAt < :expireTime
      AND NOT EXISTS (
          SELECT p FROM Payment p
          WHERE p.reservation.id = r.id
      )
    """)
    List<Reservation> findAbandonedReservations(@Param("expireTime") LocalDateTime expireTime);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reservation r SET r.status = :newStatus WHERE r.status = :oldStatus AND r.checkOut < :checkOutLimit")
    int bulkUpdateStatusByStatusAndCheckOutBefore(
            @Param("newStatus") ReservationStatus newStatus,
            @Param("oldStatus") ReservationStatus oldStatus,
            @Param("checkOutLimit") LocalDate checkOutLimit
    @Query("""
        SELECT
            r.site.id AS siteId,
            COUNT(r) AS count
        FROM Reservation r
        WHERE r.site.camping.id = :campingId
          AND r.status IN :statuses
          AND r.checkIn < :checkOut
          AND r.checkOut > :checkIn
        GROUP BY r.site.id
    """)
    List<ReservedSiteCount> findReservedCountByCampingAndPeriod(
            @Param("campingId") Long campingId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("statuses") List<ReservationStatus> statuses
    );
}
