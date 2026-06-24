package com.back.ovengers.domain.reservation.service;

import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.dto.PaymentSummaryResponse;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.dto.*;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;

    public ReservationResponse create(Long userId, ReservationRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // getter -> record accessor (get 제거)
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

        long reservedCount = reservationRepository.countOverlappingReservation(
                request.siteId(),
                request.checkIn(),
                request.checkOut(),
                ReservationStatus.CANCELLED
        );
        if (reservedCount >= site.getTotalAmount()) {
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

    @Transactional
    public ReservationCancelResponse cancelReservation(Long reservationId, Long userId) {

        // 1. 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 2. 본인 예약 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 3. 취소 가능 상태 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        // 4. CONFIRMED 상태면 결제 취소도 같이 처리
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {

            Payment payment = paymentRepository.findAllByReservation_Id(reservationId)
                    .stream()
                    .filter(p -> p.getStatus() == PaymentStatus.DONE)
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

            try {
                // 토스 결제 취소 API 호출
                tossPaymentClient.cancel(payment.getPaymentKey(), "사용자 예약 취소");
                payment.updateStatus(PaymentStatus.CANCELLED);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        // 5. 예약 취소
        reservation.updateStatus(ReservationStatus.CANCELLED);

        return ReservationCancelResponse.of(reservation);
    }
}
