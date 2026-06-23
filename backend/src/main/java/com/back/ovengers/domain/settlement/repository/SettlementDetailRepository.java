package com.back.ovengers.domain.settlement.repository;

import com.back.ovengers.domain.settlement.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    @Query("""
            SELECT sd FROM SettlementDetail sd
            JOIN FETCH sd.payment p
            JOIN FETCH p.reservation r
            JOIN FETCH r.site s
            JOIN FETCH s.camping c
            WHERE sd.settlement.id = :settlementId
            """)
    List<SettlementDetail> findBySettlementId(@Param("settlementId") Long settlementId);
}
