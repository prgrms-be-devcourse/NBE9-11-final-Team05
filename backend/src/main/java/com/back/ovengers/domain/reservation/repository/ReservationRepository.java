package com.back.ovengers.domain.reservation.repository;

import com.back.ovengers.domain.reservation.dto.HostReservationResponse;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    // 해당 날짜에 예약된 수 조회
    @Query("""
    SELECT COUNT(r) FROM Reservation r
    WHERE r.site.id = :siteId
    AND r.status != :cancelledStatus
    AND r.checkIn < :checkOut
    AND r.checkOut > :checkIn
""")
    long countOverlappingReservation(
            @Param("siteId") Long siteId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("cancelledStatus") ReservationStatus cancelledStatus
    );

    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

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
        select
            new com.back.ovengers.domain.reservation.dto.HostReservationResponse(
                r.id, c.name, s.name, r.rsvNum, r.rsvName, r.rsvPhone, r.checkIn, r.checkOut, r.guestCount, r.rsvPrice, r.status, r.createdAt
            )
            from Reservation r
               join r.site s
               join s.camping c
               where c.host.id = :hostId
                    order by r.createdAt desc
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

    List<Reservation> findAllByUserId(Long userId);
}
