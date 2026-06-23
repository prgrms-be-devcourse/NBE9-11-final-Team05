package com.back.ovengers.domain.settlement.service;

import com.back.ovengers.domain.settlement.dto.SettlementListResponse;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;

    @Transactional(readOnly = true)
    public PageResponse<SettlementListResponse> getMySettlements(Long hostId, int page) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<SettlementListResponse> responsePage = settlementRepository
                .findByHostId(hostId, pageable)
                .map(SettlementListResponse::of);

        return PageResponse.from(responsePage);
    }
}
