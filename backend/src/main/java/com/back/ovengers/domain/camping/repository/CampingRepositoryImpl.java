package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.back.ovengers.domain.camping.entity.QCamping.camping;
import static com.back.ovengers.domain.reservation.entity.QReservation.reservation;
import static com.back.ovengers.domain.site.entity.QSite.site;

@Repository
@RequiredArgsConstructor
public class CampingRepositoryImpl implements CampingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Camping> searchAvailableCampings(
            String keyword,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guestCount,
            Integer roomCount,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        // Site 조건(날짜, 인원, 객실, 가격)으로 예약 가능한 캠핑장 ID 목록 미리 추출
        // 메인 쿼리에서 중첩 서브쿼리 사용 시 성능 저하를 쿼리 분리로 해결
        List<Long> availableCampingIds = findAvailableCampingIds(
                checkIn, checkOut, guestCount, roomCount, minPrice, maxPrice
        );

        // Site 관련 조건이 하나라도 있는지 확인
        boolean hasSiteCondition = checkIn != null || checkOut != null
                || guestCount != null || roomCount != null
                || minPrice != null || maxPrice != null;

        // Site 조건이 있는데 조건을 만족하는 캠핑장이 없으면 빠르게 빈 페이지 반환
        // 불필요한 메인 쿼리 실행 방지
        if (hasSiteCondition && availableCampingIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Step 2. 기본 조건
        // - 소프트 딜리트 제외
        // - 승인된 캠핑장만 (APPROVED)
        // - Host 있는 캠핑장만 (고캠핑 API 데이터 제외, 실제 예약 가능한 캠핑장만)
        // - 키워드 검색 (이름, 도시, 지역)
        BooleanExpression baseCondition = camping.deletedAt.isNull()
                .and(camping.status.eq(CampingStatus.APPROVED))
                .and(camping.host.isNotNull())
                .and(keywordContains(keyword));

        // Site 조건이 있으면 Step 1에서 추출한 ID 목록으로 추가 필터링
        BooleanExpression condition = hasSiteCondition
                ? baseCondition.and(camping.id.in(availableCampingIds))
                : baseCondition;

        // Step 3. 최종 캠핑장 목록 조회 (최신순 정렬)
        List<Camping> content = queryFactory
                .selectFrom(camping)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(camping.createdAt.desc())
                .fetch();

        // PageableExecutionUtils: 마지막 페이지이거나 데이터 없으면 count 쿼리 생략
        return PageableExecutionUtils.getPage(content, pageable, () -> {
            Long total = queryFactory
                    .select(camping.count())
                    .from(camping)
                    .where(condition)
                    .fetchOne();
            return total != null ? total : 0L;
        });
    }

    // 조건을 만족하는 예약 가능한 캠핑장 ID 목록 추출
    // 날짜 조건이 없어도 인원/가격 조건이 있으면 필터링 적용
    // Site 단위로 잔여 수량 계산: totalAmount - 해당 날짜 예약 수 >= roomCount
    private List<Long> findAvailableCampingIds(
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guestCount,
            Integer roomCount,
            Integer minPrice,
            Integer maxPrice
    ) {
        // 날짜 조건이 있으면 해당 날짜에 겹치는 예약 수를 카운트하는 서브쿼리
        // 날짜 조건이 없으면 null로 처리 후 availableCondition에서 분기
        JPQLSubQuery<Long> reservedCountQuery = null;
        if (checkIn != null && checkOut != null) {
            reservedCountQuery = JPAExpressions
                    .select(reservation.count())
                    .from(reservation)
                    .where(
                            reservation.site.id.eq(site.id),
                            // 취소된 예약 제외
                            reservation.status.ne(ReservationStatus.CANCELLED),
                            // 날짜 겹침 체크: 체크인 < 요청 체크아웃 AND 체크아웃 > 요청 체크인
                            reservation.checkIn.lt(checkOut),
                            reservation.checkOut.gt(checkIn)
                    );
        }

        // 요청 객실 수 기본값 1 (명시 안 하면 최소 1팀 예약 가능한 캠핑장만)
        int requiredRooms = roomCount != null ? roomCount : 1;

        // 잔여 수량 조건
        // 날짜 있으면: totalAmount - 예약수 >= roomCount
        // 날짜 없으면: totalAmount >= roomCount (예약 수 0으로 간주)
        BooleanExpression availableCondition = reservedCountQuery != null
                ? site.totalAmount.subtract(reservedCountQuery).goe(requiredRooms)
                : site.totalAmount.goe(requiredRooms);

        return queryFactory
                .select(site.camping.id)
                .from(site)
                .where(
                        site.deletedAt.isNull(),
                        // 최대 인원 >= 요청 인원
                        guestCountFilter(guestCount),
                        // 가격 범위 필터
                        priceFilter(minPrice, maxPrice),
                        // 잔여 수량 조건
                        availableCondition
                )
                // 하나의 캠핑장에 여러 Site 타입이 있을 수 있으므로 중복 제거
                .distinct()
                .fetch();
    }


    // 키워드 통합 검색 (캠핑장 이름, 도시, 지역)
    // containsIgnoreCase = %keyword% (앞뒤 와일드카드) + LOWER() 함수 적용
    // 인덱스 미사용, Full Table Scan 발생 가능
    // 현재 데이터 규모에서는 허용 가능, 추후 Full-Text Search 도입 고려
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return camping.name.containsIgnoreCase(keyword)
                .or(camping.city.containsIgnoreCase(keyword))
                .or(camping.region.containsIgnoreCase(keyword));
    }

    // 최대 인원 필터: Site.maxCapacity >= 요청 인원
    private BooleanExpression guestCountFilter(Integer guestCount) {
        return guestCount != null ? site.maxCapacity.goe(guestCount) : null;
    }

    // 가격 필터: 최소/최대 가격 범위
    private BooleanExpression priceFilter(Integer minPrice, Integer maxPrice) {
        if (minPrice != null && maxPrice != null) return site.price.between(minPrice, maxPrice);
        if (minPrice != null) return site.price.goe(minPrice);
        if (maxPrice != null) return site.price.loe(maxPrice);
        return null;
    }
}