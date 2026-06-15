package com.back.ovengers.domain.reservation.repository;

import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    // site, camping 한번에 조회(리뷰에서 사용)
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.site s " +
            "JOIN FETCH s.camping " +
            "WHERE r.id = :id")
    Optional<Reservation> findByIdWithSiteAndCamping(@Param("id") Long id);

}