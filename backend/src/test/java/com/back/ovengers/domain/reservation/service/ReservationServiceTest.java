package com.back.ovengers.domain.reservation.service;

import com.back.ovengers.domain.reservation.dto.HostReservationResponse;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("호스트 예약 목록 조회 성공")
    void get_host_reservations_success() {
        // given
        Long hostId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Page<HostReservationResponse> page = new PageImpl<>(List.of());
        when(reservationRepository.findHostReservations(hostId, pageable))
                .thenReturn(page);

        // when
        Page<HostReservationResponse> result =
                reservationService.getHostReservations(hostId, pageable);

        // then
        assertThat(result).isEqualTo(page);
    }

}
