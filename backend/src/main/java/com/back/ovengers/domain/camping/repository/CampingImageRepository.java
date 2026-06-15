package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.CampingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampingImageRepository extends JpaRepository<CampingImage, Long> {
    @Modifying(clearAutomatically = true)
    @Query("delete from CampingImage c where c.camping.id = :campingId")
    void deleteByCampingId(@Param("campingId") Long campingId);
}
