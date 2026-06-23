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

    @Transactional
    public SettlementGenerateResponse generate(LocalDate settlementDate) {

        // 1. 미래 날짜 방지
        if (settlementDate.isAfter(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_DATE);
        }

        // 2. 정산 기간 계산 (해당 주 월~일)
        LocalDate startDate = settlementDate.minusDays(6);
        LocalDate endDate = settlementDate;

        // 3. 모든 호스트 조회
        List<User> hosts = userRepository.findByRole(Role.HOST);

        int generatedCount = 0;
        int totalPayoutAmount = 0;

        for (User host : hosts) {

            // 4. 정산 대상 Payment 조회
            List<Payment> payments = paymentRepository
                    .findSettlementTargets(host.getId(), startDate, endDate);

            if (payments.isEmpty()) continue;

            // 5. 금액 계산
            int totalAmount = payments.stream()
                    .mapToInt(Payment::getPaidPrice)
                    .sum();
            int feeAmount = (int) (totalAmount * FEE_RATE);
            int payoutAmount = totalAmount - feeAmount;

            // 6. Settlement 생성
            Settlement settlement = settlementRepository.save(
                    Settlement.builder()
                            .host(host)
                            .settlementDate(endDate)
                            .totalAmount(totalAmount)
                            .feeAmount(feeAmount)
                            .payoutAmount(payoutAmount)
                            .status(SettlementStatus.PENDING)
                            .build()
            );

            // 7. SettlementDetail 생성
            for (Payment payment : payments) {
                settlementDetailRepository.save(
                        SettlementDetail.builder()
                                .settlement(settlement)
                                .payment(payment)
                                .amount(payment.getPaidPrice())
                                .build()
                );
            }

            generatedCount++;
            totalPayoutAmount += payoutAmount;
        }

        return new SettlementGenerateResponse(generatedCount, settlementDate, totalPayoutAmount);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminSettlementListResponse> getAllSettlements(int page) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<AdminSettlementListResponse> responsePage = settlementRepository
                .findAllWithHost(pageable)
                .map(AdminSettlementListResponse::of);

        return PageResponse.from(responsePage);
    }

    @Transactional
    public SettlementCompleteResponse completeSettlement(Long settlementId) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND));

        if (settlement.getStatus() == SettlementStatus.COMPLETED) {
            throw new CustomException(ErrorCode.SETTLEMENT_ALREADY_COMPLETED);
        }

        settlement.complete();

        return SettlementCompleteResponse.of(settlement);
    }
}
