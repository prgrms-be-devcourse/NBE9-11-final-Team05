package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.dto.SiteCreateRequest;
import com.back.ovengers.domain.site.dto.SiteCreateResponse;
import com.back.ovengers.domain.site.dto.SiteUpdateRequest;
import com.back.ovengers.domain.site.dto.SiteUpdateResponse;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostCampingService {

    private final CampingRepository campingRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;

    @Transactional
    public CampingCreateResponse register(Long hostId, CampingCreateRequest request) {

        User host = validateHost(hostId);

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

    public List<HostCampingListResponse> getMyCampings(Long hostId) {
        validateHost(hostId);

        return campingRepository.findByHostIdAndDeletedAtIsNull(hostId)
                .stream()
                .map(HostCampingListResponse::from)
                .toList();
    }

    @Transactional
    public CampingUpdateResponse updateCamping(
            Long hostId,
            Long campingId,
            CampingUpdateRequest request
    ) {
        validateHost(hostId);
        Camping camping = getOwnedCamping(hostId, campingId);

        camping.update(request);

        return CampingUpdateResponse.from(camping);
    }

    @Transactional
    public void deleteCamping(Long hostId, Long campingId) {
        validateHost(hostId);
        Camping camping = getOwnedCamping(hostId, campingId);

        if (reservationRepository.existsBySiteCampingIdAndStatus(
                campingId,
                ReservationStatus.CONFIRMED
        )) {
            throw new CustomException(ErrorCode.CONFIRMED_RESERVATION_EXISTS);
        }

        camping.delete();
    }

    @Transactional
    public SiteCreateResponse addSite(
            Long hostId,
            Long campingId,
            SiteCreateRequest request
    ) {
        if (siteRepository.existsByCampingIdAndNameAndDeletedAtIsNull(
                campingId,
                request.name()
        )) {
            throw new CustomException(ErrorCode.DUPLICATE_SITE_NAME);
        }

        validateHost(hostId);
        Camping camping = getOwnedCamping(hostId, campingId);

        validateSiteCapacity(request);

        Site site = Site.create(camping, request);
        Site savedSite = siteRepository.save(site);

        return SiteCreateResponse.from(savedSite);
    }

    @Transactional
    public SiteUpdateResponse updateSite(
            Long hostId,
            Long siteId,
            SiteUpdateRequest request
    ) {
        validateHost(hostId);

        Site site = siteRepository.findByIdAndDeletedAtIsNull(siteId)
                .orElseThrow(() -> new CustomException(ErrorCode.SITE_NOT_FOUND));

        Camping camping = site.getCamping();

        if (!camping.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        validateSiteCapacity(request);

        if (siteRepository.existsByCampingIdAndNameAndIdNotAndDeletedAtIsNull(
                camping.getId(),
                request.name(),
                siteId
        )) {
            throw new CustomException(ErrorCode.DUPLICATE_SITE_NAME);
        }

        site.update(request);

        return SiteUpdateResponse.from(site);
    }

    @Transactional
    public void deleteSite(Long hostId, Long siteId) {
        validateHost(hostId);

        Site site = siteRepository.findByIdAndDeletedAtIsNull(siteId)
                .orElseThrow(() -> new CustomException(ErrorCode.SITE_NOT_FOUND));

        if (!site.getCamping().getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        if (reservationRepository.existsBySiteIdAndStatus(
                siteId,
                ReservationStatus.CONFIRMED
        )) {
            throw new CustomException(ErrorCode.CONFIRMED_RESERVATION_EXISTS);
        }

        site.delete();
    }

    private User validateHost(Long hostId) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (host.getRole() != Role.HOST) {
            throw new CustomException(ErrorCode.HOST_REQUIRED);
        }

        if (host.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.ALREADY_DELETED);
        }

        return host;
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

    private void validateSiteCapacity(SiteUpdateRequest request) {
        if (request.baseCapacity() != null
                && request.maxCapacity() != null
                && request.baseCapacity() > request.maxCapacity()) {
            throw new CustomException(ErrorCode.INVALID_CAPACITY);
        }
    }
}
