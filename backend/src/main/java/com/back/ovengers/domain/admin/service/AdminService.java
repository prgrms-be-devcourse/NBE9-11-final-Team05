package com.back.ovengers.domain.admin.service;


import com.back.ovengers.domain.admin.dto.AdminDashboardResponse;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CampingRepository campingRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        long totalSalesAmount = paymentRepository.getTotalSalesAmount(PaymentStatus.DONE);
        long activeUserCount = userRepository.countByStatus(Status.ACTIVE);
        long pendingCampingCount = campingRepository.countByStatus(CampingStatus.PENDING);

        return AdminDashboardResponse.of(totalSalesAmount, activeUserCount, pendingCampingCount);
    }
}
