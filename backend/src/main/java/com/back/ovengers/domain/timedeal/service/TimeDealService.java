package com.back.ovengers.domain.timedeal.service;

import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.timedeal.dto.TimeDealCreateRequest;
import com.back.ovengers.domain.timedeal.dto.TimeDealResponse;
import com.back.ovengers.domain.timedeal.dto.TimeDealUpdateRequest;
import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeDealService {

    private final TimeDealRepository timeDealRepository;
    private final SiteRepository siteRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public TimeDealResponse createTimeDeal(Long hostId, TimeDealCreateRequest request) {

        // 타임딜을 등록할 Site 조회
        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new CustomException(ErrorCode.SITE_NOT_FOUND));

        // 해당 Site가 요청한 호스트의 캠핑장인지 검증
        validateHostOwnership(site, hostId);

        // 체크인/체크아웃 날짜 유효성 검증
        validateDateRange(request.checkIn(), request.checkOut());

        // 판매 시작/종료 시간 검증
        validateSaleWindow(
                request.saleStartAt(),
                request.saleEndAt(),
                request.checkIn()
        );

        // 타임딜 가격이 정상적인 할인 가격인지 검증
        validateDiscountPrice(
                site.getPrice(),
                request.dealPrice()
        );

        // 현재 예약 및 다른 타임딜 수량을 고려하여 등록 가능 재고 검증
        validateAvailableQuantity(
                site,
                request.checkIn(),
                request.checkOut(),
                request.quantity(),
                null
        );

        // 타임딜 엔티티 생성
        TimeDeal timeDeal = TimeDeal.builder()
                .site(site)
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .quantity(request.quantity())
                .originalPrice(site.getPrice()) // 등록 시점 원가 저장
                .dealPrice(request.dealPrice())
                .saleStartAt(request.saleStartAt())
                .saleEndAt(request.saleEndAt())
                .build();

        // 저장 후 응답 DTO 변환
        return TimeDealResponse.from(
                timeDealRepository.save(timeDeal)
        );
    }

    @Transactional
    public TimeDealResponse updateTimeDeal(
            Long hostId,
            Long timeDealId,
            TimeDealUpdateRequest request
    ) {

        // 본인이 등록한 타임딜인지 확인
        TimeDeal timeDeal = getOwnedTimeDeal(
                hostId,
                timeDealId
        );

        validateSaleWindow(
                request.saleStartAt(),
                request.saleEndAt(),
                timeDeal.getCheckIn()
        );

        validateDiscountPrice(
                timeDeal.getOriginalPrice(),
                request.dealPrice()
        );

        validateAvailableQuantity(
                timeDeal.getSite(),
                timeDeal.getCheckIn(),
                timeDeal.getCheckOut(),
                request.quantity(),
                timeDeal.getId()
        );

        // 타임딜 정보 수정
        timeDeal.update(
                request.quantity(),
                request.dealPrice(),
                request.saleStartAt(),
                request.saleEndAt()
        );

        return TimeDealResponse.from(timeDeal);
    }

    @Transactional
    public void cancelTimeDeal(Long hostId, Long timeDealId) {

        TimeDeal timeDeal = getOwnedTimeDeal(
                hostId,
                timeDealId
        );

        timeDeal.cancel();
    }

    /**
     * 타임딜 삭제
     *
     * 판매 이력이 없는 경우에만 Soft Delete 처리한다.
     */
    @Transactional
    public void deleteTimeDeal(Long hostId, Long timeDealId) {

        TimeDeal timeDeal = getOwnedTimeDeal(
                hostId,
                timeDealId
        );

        // 판매된 타임딜은 이력 보존을 위해 삭제 불가
        if (timeDeal.getSoldCount() > 0) {
            throw new CustomException(
                    ErrorCode.TIME_DEAL_ALREADY_SOLD
            );
        }

        timeDeal.softDelete();
    }

    //호스트가 등록한 타임딜 목록 조회
    public Page<TimeDealResponse> getMyTimeDeals(
            Long hostId,
            Pageable pageable
    ) {

        return timeDealRepository
                .findAllByHostId(hostId, pageable)
                .map(TimeDealResponse::from);
    }

    //현재 판매 중인 타임딜 목록 조회
    public Page<TimeDealResponse> getActiveTimeDeals(
            Pageable pageable
    ) {

        return timeDealRepository
                .findAllActive(LocalDateTime.now(), pageable)
                .map(TimeDealResponse::from);
    }

    //타임딜 상세 조회
    public TimeDealResponse getTimeDeal(Long timeDealId) {

        TimeDeal timeDeal = timeDealRepository
                .findActiveById(timeDealId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.TIME_DEAL_NOT_FOUND
                        )
                );

        return TimeDealResponse.from(timeDeal);
    }


    /**
     * 타임딜 존재 여부 및 소유권 검증
     *
     * 타임딜이 존재하는지 확인하고
     * 요청한 호스트가 실제 소유자인지 검증한다.
     */
    private TimeDeal getOwnedTimeDeal(
            Long hostId,
            Long timeDealId
    ) {

        // 타임딜 조회
        TimeDeal timeDeal = timeDealRepository
                .findActiveById(timeDealId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.TIME_DEAL_NOT_FOUND
                        )
                );

        // 요청한 호스트가 소유자가 아니면 접근 거부
        if (!timeDeal.isOwnedBy(hostId)) {
            throw new CustomException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        return timeDeal;
    }

    //Site 소유 호스트 검증
    private void validateHostOwnership(
            Site site,
            Long hostId
    ) {

        if (!site.getCamping().getHost().getId().equals(hostId)) {
            throw new CustomException(
                    ErrorCode.ACCESS_DENIED
            );
        }
    }


    /**
     * 체크인/체크아웃 날짜 검증
     *
     * - 체크아웃은 체크인 이후여야 함
     * - 과거 날짜는 등록 불가
     */
    private void validateDateRange(
            LocalDate checkIn,
            LocalDate checkOut
    ) {

        if (!checkIn.isBefore(checkOut)) {
            throw new CustomException(
                    ErrorCode.TIME_DEAL_INVALID_DATE_RANGE
            );
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new CustomException(
                    ErrorCode.TIME_DEAL_PAST_DATE
            );
        }
    }


    /**
     * 판매 기간 검증
     *
     * - 판매 시작 시간 < 판매 종료 시간
     * - 판매 종료 시간은 체크인 이전이어야 함
     */
    private void validateSaleWindow(
            LocalDateTime start,
            LocalDateTime end,
            LocalDate checkIn
    ) {

        if (!start.isBefore(end)) {
            throw new CustomException(
                    ErrorCode.TIME_DEAL_INVALID_SALE_WINDOW
            );
        }

        if (end.isAfter(checkIn.atStartOfDay())) {
            throw new CustomException(
                    ErrorCode.TIME_DEAL_INVALID_SALE_WINDOW
            );
        }
    }

    /**
     * 할인 가격 검증
     *
     * 타임딜 가격은 원가보다 낮아야 한다.
     */
    private void validateDiscountPrice(
            int originalPrice,
            int dealPrice
    ) {

        if (dealPrice >= originalPrice) {
            throw new CustomException(
                    ErrorCode.TIME_DEAL_INVALID_PRICE
            );
        }
    }

    /**
     * 등록 가능 재고 검증
     *
     * 가용 재고 =
     * Site 총 재고
     * - 이미 예약된 수량
     * - 다른 타임딜이 선점한 수량
     */
    private void validateAvailableQuantity(
            Site site,
            LocalDate checkIn,
            LocalDate checkOut,
            int requestedQuantity,
            Long excludeTimeDealId
    ) {

        // 해당 기간에 이미 예약된 수량 조회
        long reservedCount =
                reservationRepository
                        .countActiveReservationsByOverlappingDates(
                                site.getId(),
                                checkIn,
                                checkOut
                        );

        // 해당 기간에 다른 타임딜이 선점한 수량 조회
        int allocatedByOtherDeals =
                timeDealRepository
                        .sumReservedQuantityByOverlappingDates(
                                site.getId(),
                                checkIn,
                                checkOut,
                                excludeTimeDealId
                        );

        // 실제 등록 가능한 수량 계산
        int available =
                site.getTotalAmount()
                        - (int) reservedCount
                        - allocatedByOtherDeals;

        // 요청 수량이 가용 재고보다 많으면 예외 발생
        if (requestedQuantity > available) {
            throw new CustomException(
                    ErrorCode.TIME_DEAL_STOCK_EXCEEDED
            );
        }
    }
}