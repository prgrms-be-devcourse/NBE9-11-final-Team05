package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.CampingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CampingImageRepository extends JpaRepository<CampingImage, Long> {
    @Query("select distinct ci.camping.id from CampingImage ci")
    Set<Long> findCampingIdsWithImages();

    Optional<CampingImage> findByIdAndCampingId(Long imageId, Long campingId);

    List<CampingImage> findByCampingId(Long campingId);

    Optional<CampingImage> findFirstByCampingIdAndIdNotOrderByIdAsc(
            Long campingId,
            Long imageId
    );

    // campingId와 imageUrl만 조회 (LAZY 로딩 방지)
    // Object[0] = camping_id, Object[1] = image_url
    @Query("SELECT ci.camping.id, ci.imageUrl FROM CampingImage ci WHERE ci.camping.id IN :campingIds")
    List<Object[]> findImagesByCampingIds(@Param("campingIds") List<Long> campingIds);
}
