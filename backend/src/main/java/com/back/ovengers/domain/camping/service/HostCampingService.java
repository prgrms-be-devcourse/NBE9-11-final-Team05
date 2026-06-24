package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.dto.SiteCreateRequest;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostCampingService {

    private final CampingRepository campingRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;
    private final CampingImageRepository campingImageRepository;

    @Transactional
    public CampingCreateResponse register(Long hostId, CampingCreateRequest request) {

        User host = userRepository.getReferenceById(hostId);

        validateDuplicateSiteNameInRequest(request.sites());

        Camping camping = Camping.create(host, request);
        Camping savedCamping = campingRepository.save(camping);

        List<Site> sites = request.sites().stream()
                .peek(this::validateSiteCapacity)
                .map(siteRequest -> Site.create(savedCamping, siteRequest))
                .toList();
        siteRepository.saveAll(sites);

        return new CampingCreateResponse(
                savedCamping.getId(),
                savedCamping.getName(),
                savedCamping.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<HostCampingListResponse> getMyCampings(Long hostId) {

        return campingRepository.findByHostIdAndDeletedAtIsNull(hostId)
                .stream()
                .map(HostCampingListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HostCampingDetailResponse getMyCampingDetail(
            Long hostId,
            Long campingId
    ) {
        Camping camping = getOwnedCamping(hostId, campingId);

        return HostCampingDetailResponse.from(camping);
    }

    @Transactional
    public CampingUpdateResponse updateCamping(
            Long hostId,
            Long campingId,
            CampingUpdateRequest request
    ) {
        Camping camping = getOwnedCamping(hostId, campingId);

        camping.update(request);

        return CampingUpdateResponse.from(camping);
    }

    @Transactional
    public void deleteCamping(Long hostId, Long campingId) {

        Camping camping = getOwnedCamping(hostId, campingId);

        if (reservationRepository.existsBySiteCampingIdAndStatus(
                campingId,
                ReservationStatus.CONFIRMED
        )) {
            throw new CustomException(ErrorCode.CONFIRMED_RESERVATION_EXISTS);
        }

        camping.delete();
    }

    @Transactional(readOnly = true)
    public List<CampingClaimSearchResponse> searchClaimableCampings(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        // 아직 호스트가 연결되지 않은 고캠핑 캠핑장만 검색 대상으로 반환
        return campingRepository.searchClaimableCampings(keyword.trim())
                .stream()
                .map(CampingClaimSearchResponse::from)
                .toList();
    }

    @Transactional
    public void claimCamping(Long hostId, CampingClaimRequest request) {
        User host = userRepository.getReferenceById(hostId);

        Camping camping = campingRepository
                .findByIdAndDeletedAtIsNull(request.campingId())
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        // 고캠핑에서 제공하는 관광사업자번호와 사용자가 입력한 번호가 일치해야 소유권 인증 가능
        if (!Objects.equals(camping.getTourNum(), request.tourNum())) {
            throw new CustomException(ErrorCode.INVALID_TOUR_NUMBER);
        }

        // 이미 다른 호스트가 등록한 캠핑장인지 검증 후 host 할당
        camping.assignHost(host);
    }

    private Camping getOwnedCamping(Long hostId, Long campingId) {
        Camping camping = campingRepository.findByIdAndDeletedAtIsNull(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        if (!camping.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        return camping;
    }

    private void validateSiteCapacity(SiteCreateRequest request) {
        if (request.baseCapacity() > request.maxCapacity()) {
            throw new CustomException(ErrorCode.INVALID_CAPACITY);
        }
    }

    private void validateDuplicateSiteNameInRequest(List<SiteCreateRequest> sites) {
        long uniqueNameCount = sites.stream()
                .map(SiteCreateRequest::name)
                .distinct()
                .count();

        if (uniqueNameCount < sites.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_SITE_NAME);
        }
    }
}
