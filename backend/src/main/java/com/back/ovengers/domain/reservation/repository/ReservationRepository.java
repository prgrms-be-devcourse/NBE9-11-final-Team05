package com.back.ovengers.domain.reservation.repository;

import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

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

}