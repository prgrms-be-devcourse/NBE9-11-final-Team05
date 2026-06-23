package com.back.ovengers.domain.admin.service;


import com.back.ovengers.domain.admin.dto.AdminDashboardResponse;
import com.back.ovengers.domain.admin.dto.AdminPendingCampingResponse;
import com.back.ovengers.domain.admin.dto.CampingBulkApproveRequest;
import com.back.ovengers.domain.admin.dto.CampingRejectRequest;
import com.back.ovengers.domain.admin.dto.PendingCampingResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.event.CampingApprovedEvent;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.notification.entity.NotificationType;
import com.back.ovengers.domain.notification.service.NotificationService;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CampingRepository campingRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        long totalSalesAmount = paymentRepository.getTotalSalesAmount(PaymentStatus.DONE);
        long activeUserCount = userRepository.countByStatus(Status.ACTIVE);
        long pendingCampingCount = campingRepository.countByStatus(CampingStatus.PENDING);

        return AdminDashboardResponse.of(totalSalesAmount, activeUserCount, pendingCampingCount);
    }

    @Transactional(readOnly = true)
    public AdminPendingCampingResponse getPendingCampingList(Pageable pageable) {
        Page<Camping> campingList = campingRepository.findByStatusWithHost(CampingStatus.PENDING, pageable);
        Page<PendingCampingResponse> responsePage = campingList.map(PendingCampingResponse::from);
        return AdminPendingCampingResponse.of(campingList.getTotalElements(), responsePage);
    }

    // 단건 승인
    public void approveCamping(Long campingId) {
        Camping camping = campingRepository.findById(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        camping.approve();

        eventPublisher.publishEvent(
                new CampingApprovedEvent(camping.getId(), camping.getName())
        );

        // 호스트에게 알림
        notificationService.send(
                camping.getHost(),
                NotificationType.CAMPING_APPROVED,
                "캠핑장 '" + camping.getName() + "'이 승인되었습니다."
        );
    }

    // 단건 거절
    public void rejectCamping(Long campingId, CampingRejectRequest request) {
        Camping camping = campingRepository.findById(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));
        //TODO: DB 저장을 어떤식으로 할지, 현재는 따로 저장 x

        // 호스트에게 알림
        notificationService.send(
                camping.getHost(),
                NotificationType.CAMPING_REJECTED,
                "캠핑장 '" + camping.getName() + "'이 거절되었습니다."
        );

        camping.reject();
    }

    // 일괄 승인
    public void approveCampingList(CampingBulkApproveRequest request) {

        List<Camping> campingList = campingRepository.findByIdIn(request.campingIds());

        // 요청한 ID 개수랑 실제 조회된 개수가 다르면 존재하지 않는 ID 포함된 것
        if (campingList.size() != request.campingIds().size()) {
            throw new CustomException(ErrorCode.CAMPING_NOT_FOUND);
        }

        campingList.forEach(camping -> {
            // 승인
            camping.approve();
            // 각 호스트에게 알림
            notificationService.send(
                    camping.getHost(),
                    NotificationType.CAMPING_APPROVED,
                    "캠핑장 '" + camping.getName() + "'이 승인되었습니다."
            );
        });

    }
}
