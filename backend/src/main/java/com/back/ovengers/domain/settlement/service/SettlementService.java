package com.back.ovengers.domain.settlement.service;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.settlement.dto.*;
import com.back.ovengers.domain.settlement.entity.Settlement;
import com.back.ovengers.domain.settlement.entity.SettlementDetail;
import com.back.ovengers.domain.settlement.entity.SettlementStatus;
import com.back.ovengers.domain.settlement.repository.SettlementDetailRepository;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private static final double FEE_RATE = 0.1;

    @Transactional(readOnly = true)
    public PageResponse<SettlementListResponse> getMySettlements(Long hostId, int page) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<SettlementListResponse> responsePage = settlementRepository
                .findByHostId(hostId, pageable)
                .map(SettlementListResponse::of);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public SettlementDetailWithListResponse getSettlementDetail(Long settlementId, Long hostId) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND));

        // 본인 정산인지 확인
        if (!settlement.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        List<SettlementDetailResponse> details = settlementDetailRepository
                .findBySettlementId(settlementId)
                .stream()
                .map(SettlementDetailResponse::of)
                .toList();

        return SettlementDetailWithListResponse.of(settlement, details);
    }


}
