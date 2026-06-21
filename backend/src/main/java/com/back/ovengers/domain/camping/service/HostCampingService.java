package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
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
public class HostCampingService {

    private final CampingRepository campingRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;
    private final CampingImageRepository campingImageRepository;

    @Transactional
    public CampingCreateResponse register(Long hostId, CampingCreateRequest request) {

        User host = validateHost(hostId);

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
        validateHost(hostId);

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
        validateHost(hostId);
        Camping camping = getOwnedCamping(hostId, campingId);

        return HostCampingDetailResponse.from(camping);
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
    public CampingImageCreateResponse addCampingImage(
            Long hostId,
            Long campingId,
            CampingImageCreateRequest request
    ) {
        validateHost(hostId);

        Camping camping = campingRepository.findByIdAndDeletedAtIsNull(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        if (!camping.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        CampingImage image = CampingImage.from(camping, request.imageUrl());

        CampingImage savedImage = campingImageRepository.save(image);

        return CampingImageCreateResponse.from(savedImage);
    }

    @Transactional
    public void deleteCampingImage(
            Long hostId,
            Long campingId,
            Long imageId
    ) {
        validateHost(hostId);

        Camping camping = campingRepository.findByIdAndDeletedAtIsNull(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        if (!camping.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        CampingImage image = campingImageRepository.findByIdAndCampingId(imageId, campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_IMAGE_NOT_FOUND));

        campingImageRepository.delete(image);
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
