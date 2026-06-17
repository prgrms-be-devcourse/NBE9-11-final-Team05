package com.back.ovengers.domain.reservation.service;

import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.reservation.dto.ReservationDetailResponse;
import com.back.ovengers.domain.reservation.dto.ReservationRequest;
import com.back.ovengers.domain.reservation.dto.ReservationResponse;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
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

    public ReservationResponse create(Long userId, ReservationRequest request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 날짜 유효성 검증
        if (!request.getCheckIn().isBefore(request.getCheckOut())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }
        if (request.getCheckIn().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }

        // 2. 구역 조회 - 비관적 락 적용
        Site site = siteRepository.findByIdWithLock(request.getSiteId())
                .orElseThrow(() -> new CustomException(ErrorCode.SITE_NOT_FOUND));

        // 2-1. 삭제된 구역 체크
        if (site.isDeleted()) {
            throw new CustomException(ErrorCode.SITE_NOT_FOUND);
        }

        // 2-2. 캠핑장 승인 상태 체크
        if (site.getCamping().getStatus() != CampingStatus.APPROVED) {
            throw new CustomException(ErrorCode.CAMPING_NOT_AVAILABLE);
        }

        // 2-3. 인원수 검증
        if (request.getGuestCount() > site.getMaxCapacity()) {
            throw new CustomException(ErrorCode.GUEST_COUNT_EXCEEDED);
        }


        // 3. 재고 확인 (중복 확인)
        long reservedCount = reservationRepository.countOverlappingReservation(
                request.getSiteId(),
                request.getCheckIn(),
                request.getCheckOut(),
                ReservationStatus.CANCELLED
        );
        if (reservedCount >= site.getTotalAmount()) {
            throw new CustomException(ErrorCode.SITE_NOT_AVAILABLE);
        }

        // 4. 예약 번호 생성
        // 영문 3글자 랜덤 + 날짜 + 랜덤 UUID로 문자열 처리
        String prefix = RandomStringUtils.randomAlphabetic(3).toUpperCase();
        String rsvNum = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 5. 가격 계산
        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        int rsvPrice = (int) (site.getPrice() * days);

        // 6. 예약 저장
        Reservation reservation = Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum(rsvNum)
                .rsvName(request.getRsvName())
                .rsvPhone(request.getRsvPhone())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .guestCount(request.getGuestCount())
                .rsvPrice(rsvPrice)
                .request(request.getRequest())
                .status(ReservationStatus.PENDING) // 결제 완료 후 PENDING -> CONFIRMED
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
    public List<ReservationResponse> getMyReservations(Long userId, int page) {

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, 10);  // 한 페이지 10개

        return reservationRepository.findByUserIdOrderByCreatedAtDesc(
                        userId, pageable)
                .stream()
                .map(ReservationResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getSummary(Long reservationId, Long userId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return ReservationResponse.of(reservation);
    }
}
