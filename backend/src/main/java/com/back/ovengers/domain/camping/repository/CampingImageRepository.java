package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.CampingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CampingImageRepository extends JpaRepository<CampingImage, Long> {
    @Modifying(clearAutomatically = true)
    @Query("delete from CampingImage c where c.camping.id = :campingId")
    void deleteByCampingId(@Param("campingId") Long campingId);

    @Query("select distinct ci.camping.id from CampingImage ci")
    Set<Long> findCampingIdsWithImages();

    Optional<CampingImage> findByIdAndCampingId(Long imageId, Long campingId);

    List<CampingImage> findByCampingId(Long campingId);
}
