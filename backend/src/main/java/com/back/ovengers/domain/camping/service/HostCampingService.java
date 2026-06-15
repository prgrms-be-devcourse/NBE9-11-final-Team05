package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingCreateRequest;
import com.back.ovengers.domain.camping.dto.CampingCreateResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostCampingService {

    private final CampingRepository campingRepository;
    private final UserRepository userRepository;

    @Transactional
    public CampingCreateResponse register(Long hostId, CampingCreateRequest request) {

        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (host.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.ALREADY_DELETED);
        }

        if (host.getRole() != Role.HOST) {
            throw new CustomException(ErrorCode.HOST_REQUIRED);
        }

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
}
