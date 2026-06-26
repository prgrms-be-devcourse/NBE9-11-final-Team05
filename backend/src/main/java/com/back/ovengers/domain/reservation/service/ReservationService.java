package com.back.ovengers.domain.reservation.service;

import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.dto.PaymentSummaryResponse;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.dto.*;
import com.back.ovengers.domain.reservation.dto.*;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.entity.TimeDealStatus;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final TimeDealRepository timeDealRepository;

    public ReservationResponse create(Long userId, ReservationRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        if (!request.checkIn().isBefore(request.checkOut())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }
        if (request.checkIn().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }

        Site site = siteRepository.findByIdWithLock(request.siteId())
                .orElseThrow(() -> new CustomException(ErrorCode.SITE_NOT_FOUND));

        if (site.isDeleted()) {
            throw new CustomException(ErrorCode.SITE_NOT_FOUND);
        }

        if (site.getCamping().getStatus() != CampingStatus.APPROVED) {
            throw new CustomException(ErrorCode.CAMPING_NOT_AVAILABLE);
        }

        if (request.guestCount() > site.getMaxCapacity()) {
            throw new CustomException(ErrorCode.GUEST_COUNT_EXCEEDED);
        }


        // Reservation 비관적 락 + 재고 확인
        List<Reservation> overlapping = reservationRepository.findOverlappingReservationsWithLock(
                request.siteId(),
                request.checkIn(),
                request.checkOut(),
                ReservationStatus.CANCELLED
        );
        if (overlapping.size() >= site.getTotalAmount()) {
            throw new CustomException(ErrorCode.SITE_NOT_AVAILABLE);
        }

        String prefix = RandomStringUtils.randomAlphabetic(3).toUpperCase();
        String rsvNum = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        long days = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        int rsvPrice = (int) (site.getPrice() * days);

        Reservation reservation = Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum(rsvNum)
                .rsvName(request.rsvName())
                .rsvPhone(request.rsvPhone())
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .guestCount(request.guestCount())
                .rsvPrice(rsvPrice)
                .request(request.request())
                .status(ReservationStatus.PENDING)
                .build();

        return ReservationResponse.of(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationDetailResponse getReservation(Long reservationId, Long userId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 본인 예약인지 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return ReservationDetailResponse.of(reservation);
    }

    // 사용자 예약 목록 조회 - 페이지당 10개
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getMyReservations(Long userId, int page) {

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, 10);

        Page<ReservationResponse> responsePage = reservationRepository
                .findByUserIdWithDetails(userId, pageable)
                .map(ReservationResponse::of);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getSummary(Long reservationId, Long userId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return PaymentSummaryResponse.of(reservation);
    }

    @Transactional(readOnly = true)
    public Page<HostReservationResponse> getHostReservations(Long hostId, Pageable pageable) {
        return reservationRepository.findHostReservations(hostId, pageable);
    }

    public ReservationCancelResponse cancelReservation(Long reservationId, Long userId) {

        // 1. 예약 조회
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 2. 본인 예약 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 3. 취소 가능 상태 확인
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.RESERVATION_CANNOT_BE_CANCELLED);
        }

        // 4. DONE 상태 결제 조회
        Payment payment = paymentRepository.findByReservation_IdAndStatus(reservationId, PaymentStatus.DONE)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 5. 토스 결제 취소 API 호출 (트랜잭션 밖에서 실행)
        cancelPaymentOutsideTransaction(payment);

        // 6. 예약/결제 상태 변경 (트랜잭션 안에서 처리)
        payment.updateStatus(PaymentStatus.CANCELLED);
        reservation.updateStatus(ReservationStatus.CANCELLED);

        return ReservationCancelResponse.of(reservation);
    }

    // 트랜잭션 밖에서 토스 API 호출
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void cancelPaymentOutsideTransaction(Payment payment) {
        try {
            tossPaymentClient.cancel(payment.getPaymentKey(), "사용자 예약 취소");
        } catch (Exception e) {
            log.error("토스 결제 취소 실패 - paymentKey: {}, error: {}",
                    payment.getPaymentKey(), e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 타임딜 예약 생성
     *
     * [트랜잭션 설계]
     * purchaseAtomically()와 reservationRepository.save()가 같은 트랜잭션 안에 있다.
     *
     * 덕분에:
     * - 예약 저장 중 예외 발생 시 → 트랜잭션 롤백 → soldCount도 같이 롤백
     * - soldCount가 증가했는데 예약이 안 생기는 불일치 상황이 원천 차단됨
     *
     * [흐름]
     * 1. 유저 조회
     * 2. 타임딜 조회 및 상태 검증 (ACTIVE 여부)
     * 3. 인원수 검증 (Site의 maxCapacity)
     * 4. purchaseAtomically() → soldCount 원자적 증가 (실패 시 재고 부족/비활성 예외)
     * 5. 예약 저장 (가격 = dealPrice × 박수)
     * 6. SOLD_OUT 전환 체크
     *
     * [가격 계산]
     * dealPrice는 1박 기준 할인가.
     * 기존 일반 예약(site.getPrice() × days)과 동일한 방식으로 계산한다.
     *
     * @param userId      예약자 ID
     * @param timeDealId  구매할 타임딜 ID
     * @param request     예약자 정보 (이름, 연락처, 인원, 요청사항)
     */
    public ReservationResponse createTimeDealReservation(
            Long userId,
            Long timeDealId,
            TimeDealReservationRequest request
    ) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 타임딜 조회
        TimeDeal timeDeal = timeDealRepository.findActiveById(timeDealId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIME_DEAL_NOT_FOUND));

        // 3. 타임딜 상태 검증
        if (timeDeal.getStatus() == TimeDealStatus.SOLD_OUT) {
            throw new CustomException(ErrorCode.TIME_DEAL_SOLD_OUT);
        }
        if (timeDeal.getStatus() != TimeDealStatus.ACTIVE) {
            throw new CustomException(ErrorCode.TIME_DEAL_NOT_ACTIVE);
        }

        // 4. 인원수 검증
        Site site = timeDeal.getSite();
        if (request.guestCount() > site.getMaxCapacity()) {
            throw new CustomException(ErrorCode.GUEST_COUNT_EXCEEDED);
        }

        // 5. 캠핑장 운영 상태 검증
        if (site.getCamping().getStatus() != CampingStatus.APPROVED) {
            throw new CustomException(ErrorCode.CAMPING_NOT_AVAILABLE);
        }

        // 6. soldCount 원자적 증가
        //    WHERE status = 'ACTIVE' AND soldCount + 1 <= quantity 조건이 포함되어 있어
        //    재고 초과나 상태 불일치를 DB 레벨에서 방어한다.
        int affectedRows = timeDealRepository.purchaseAtomically(timeDealId, 1, TimeDealStatus.ACTIVE);

        if (affectedRows == 0) {
            // 실패 원인 재조회
            TimeDeal fresh = timeDealRepository.findActiveById(timeDealId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TIME_DEAL_NOT_FOUND));

            if (fresh.getStatus() == TimeDealStatus.SOLD_OUT) {
                throw new CustomException(ErrorCode.TIME_DEAL_SOLD_OUT);
            }
            if (fresh.getStatus() != TimeDealStatus.ACTIVE) {
                throw new CustomException(ErrorCode.TIME_DEAL_NOT_ACTIVE);
            }
            // ACTIVE이지만 재고 부족 (마지막 자리가 방금 팔림)
            throw new CustomException(ErrorCode.TIME_DEAL_STOCK_EXCEEDED);
        }

        // 7. 예약 저장
        //    가격 = dealPrice(1박 할인가) × 박수
        long days = ChronoUnit.DAYS.between(timeDeal.getCheckIn(), timeDeal.getCheckOut());
        int rsvPrice = (int) (timeDeal.getDealPrice() * days);

        Reservation reservation = Reservation.builder()
                .user(user)
                .site(site)
                .timeDeal(timeDeal)           // 타임딜 연결
                .rsvNum(generateRsvNum())
                .rsvName(request.rsvName())
                .rsvPhone(request.rsvPhone())
                .checkIn(timeDeal.getCheckIn())   // 날짜는 타임딜에서 가져옴
                .checkOut(timeDeal.getCheckOut())
                .guestCount(request.guestCount())
                .rsvPrice(rsvPrice)
                .request(request.request())
                .status(ReservationStatus.PENDING)
                .build();

        ReservationResponse response = ReservationResponse.of(reservationRepository.save(reservation));

        // 8. 마지막 재고 소진 시 SOLD_OUT 전환
        timeDealRepository.markSoldOutIfExhausted(
                timeDealId, TimeDealStatus.SOLD_OUT, TimeDealStatus.ACTIVE
        );

        return response;
    }
    private String generateRsvNum() {
        String prefix = RandomStringUtils.randomAlphabetic(3).toUpperCase();
        return prefix + "-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
