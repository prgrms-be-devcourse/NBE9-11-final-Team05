package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.Camping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampingRepository extends JpaRepository<Camping, Long> {

    List<Camping> findByHostIdAndDeletedAtIsNull(Long hostId);

    Optional<Camping> findByIdAndDeletedAtIsNull(Long campingId);

    // 소프트 딜리트 되지 않은 캠핑장 존재 여부 확인
    boolean existsByIdAndDeletedAtIsNull(Long id);
}
