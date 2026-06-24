package com.back.ovengers.domain.settlement.service;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
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
import java.util.Map;
import java.util.stream.Collectors;

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


        // 3. 정산 대상 Payment 한 번에 조회 (N+1 해결)
        List<Payment> payments = paymentRepository.findAllSettlementTargets(
                startDate, endDate, PaymentStatus.DONE);

        if (payments.isEmpty()) {
            return new SettlementGenerateResponse(0, settlementDate, 0);
        }

        // 4. 호스트별로 그룹핑
        Map<User, List<Payment>> groupedByHost = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getReservation().getSite().getCamping().getHost()
                ));

        int generatedCount = 0;
        int totalPayoutAmount = 0;

        for (Map.Entry<User, List<Payment>> entry : groupedByHost.entrySet()) {
            User host = entry.getKey();
            List<Payment> hostPayments = entry.getValue();

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
