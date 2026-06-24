package com.back.ovengers.domain.settlement.repository;

import com.back.ovengers.domain.settlement.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query(value = """
            SELECT s FROM Settlement s
            WHERE s.host.id = :hostId
            ORDER BY s.settlementDate DESC
            """,
            countQuery = """
            SELECT COUNT(s) FROM Settlement s
            WHERE s.host.id = :hostId
            """)
    Page<Settlement> findByHostId(@Param("hostId") Long hostId, Pageable pageable);

    @Query(value = """
        SELECT s FROM Settlement s
        JOIN FETCH s.host h
        ORDER BY s.settlementDate DESC
        """,
            countQuery = """
        SELECT COUNT(s) FROM Settlement s
        """)
    Page<Settlement> findAllWithHost(Pageable pageable);
}