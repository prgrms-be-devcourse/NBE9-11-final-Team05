package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.Camping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface CampingRepositoryCustom {
    Page<Camping> searchAvailableCampings(
            String keyword,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guestCount,
            Integer roomCount,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    );
}
