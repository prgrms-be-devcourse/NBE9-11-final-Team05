package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
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

    @Transactional
    public CampingCreateResponse register(Long hostId, CampingCreateRequest request) {

        User host = validateHost(hostId);

        Camping camping = Camping.builder()
                .host(host)
                .tourNum(request.tourNum())
                .businessNum(request.businessNum())
                .name(request.name())
                .region(request.region())
                .city(request.city())
                .address(request.address())
                .status(CampingStatus.PENDING)
                .build();

        Camping savedCamping = campingRepository.save(camping);

        return new CampingCreateResponse(
                savedCamping.getId(),
                savedCamping.getStatus()
        );
    }

    public List<HostCampingListResponse> getMyCampings(Long hostId) {
        validateHost(hostId);

        return campingRepository.findByHostIdAndDeletedAtIsNull(hostId)
                .stream()
                .map(camping -> new HostCampingListResponse(
                        camping.getId(),
                        camping.getName(),
                        camping.getRegion(),
                        camping.getCity(),
                        camping.getAddress(),
                        camping.getFirstImageUrl(),
                        camping.getRating()
                ))
                .toList();
    }

    @Transactional
    public CampingUpdateResponse updateCamping(
            Long hostId,
            Long campingId,
            CampingUpdateRequest request
    ) {
        validateHost(hostId);

        Camping camping = campingRepository.findByIdAndDeletedAtIsNull(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        if (!camping.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        camping.update(request);

        return CampingUpdateResponse.from(camping);
    }

    @Transactional
    public void deleteCamping(Long hostId, Long campingId) {
        validateHost(hostId);

        Camping camping = campingRepository.findByIdAndDeletedAtIsNull(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        if (!camping.getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }

        if (reservationRepository.existsReservationByCampingIdAndStatus(
                campingId,
                ReservationStatus.CONFIRMED
        )) {
            throw new CustomException(ErrorCode.CONFIRMED_RESERVATION_EXISTS);
        }

        camping.delete();
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
}
