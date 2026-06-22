package com.back.ovengers.domain.site.service;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.dto.*;
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
public class HostSiteService {

    private final CampingRepository campingRepository;
    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;

    public List<HostSiteResponse> getSites(
            Long hostId,
            Long campingId
    ) {
        getOwnedCamping(hostId, campingId);

        return siteRepository
                .findByCampingIdAndDeletedAtIsNullOrderByIdAsc(campingId)
                .stream()
                .map(HostSiteResponse::from)
                .toList();
    }

    @Transactional
    public SiteCreateResponse addSite(
            Long hostId,
            Long campingId,
            SiteCreateRequest request
    ) {
        Camping camping = getOwnedCamping(hostId, campingId);

        if (siteRepository.existsByCampingIdAndNameAndDeletedAtIsNull(
                campingId,
                request.name()
        )) {
            throw new CustomException(ErrorCode.DUPLICATE_SITE_NAME);
        }

        validateSiteCapacity(request);

        Site site = Site.create(camping, request);
        Site savedSite = siteRepository.save(site);

        return SiteCreateResponse.from(savedSite);
    }

    @Transactional
    public SiteUpdateResponse updateSite(
            Long hostId,
            Long campingId,
            Long siteId,
            SiteUpdateRequest request
    ) {
        Site site = getOwnedSite(hostId, campingId, siteId);
        Camping camping = site.getCamping();

        validateSiteCapacity(site, request);

        if (request.name() != null && siteRepository.existsByCampingIdAndNameAndIdNotAndDeletedAtIsNull(
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
    public void deleteSite(
            Long hostId,
            Long campingId,
            Long siteId
    ) {
        Site site = getOwnedSite(hostId, campingId, siteId);

        if (reservationRepository.existsBySiteIdAndStatus(
                siteId,
                ReservationStatus.CONFIRMED
        )) {
            throw new CustomException(ErrorCode.CONFIRMED_RESERVATION_EXISTS);
        }

        site.delete();
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

    private void validateSiteCapacity(Site site, SiteUpdateRequest request) {
        Integer baseCapacity = request.baseCapacity() != null
                ? request.baseCapacity()
                : site.getBaseCapacity();

        Integer maxCapacity = request.maxCapacity() != null
                ? request.maxCapacity()
                : site.getMaxCapacity();

        if (baseCapacity > maxCapacity) {
            throw new CustomException(ErrorCode.INVALID_CAPACITY);
        }
    }

    private Site getOwnedSite(
            Long hostId,
            Long campingId,
            Long siteId
    ) {
        Site site = siteRepository.findByIdAndDeletedAtIsNull(siteId)
                .orElseThrow(() -> new CustomException(ErrorCode.SITE_NOT_FOUND));

        Camping camping = site.getCamping();

        if (!camping.getId().equals(campingId)) {
            throw new CustomException(ErrorCode.SITE_NOT_FOUND);
        }

        if (!camping.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        return site;
    }
}
