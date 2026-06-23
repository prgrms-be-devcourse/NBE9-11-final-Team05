package com.back.ovengers.domain.settlement.repository;

import com.back.ovengers.domain.settlement.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    List<SettlementDetail> findBySettlementId(Long settlementId);
}
