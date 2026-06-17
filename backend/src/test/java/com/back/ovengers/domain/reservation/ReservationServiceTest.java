package com.back.ovengers.domain.reservation;

import com.back.ovengers.domain.reservation.dto.HostReservationResponse;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.UserFixture;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        User host = UserFixture.host().build();
        ReflectionTestUtils.setField(host, "id", hostId);

        Page<HostReservationResponse> page = new PageImpl<>(List.of());

        when(userRepository.findByIdAndDeletedAtIsNull(hostId))
                .thenReturn(Optional.of(host));

        when(reservationRepository.findHostReservations(hostId, pageable))
                .thenReturn(page);

        // when
        Page<HostReservationResponse> result =
                reservationService.getHostReservations(hostId, pageable);

        // then
        assertThat(result).isEqualTo(page);
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 예외 발생")
    void get_host_reservations_user_not_found() {
        // given
        Long hostId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByIdAndDeletedAtIsNull(hostId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> reservationService.getHostReservations(hostId, pageable)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("호스트가 아니면 예외 발생")
    void get_host_reservations_role_invalid() {
        // given
        Long hostId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = UserFixture.user().build();
        ReflectionTestUtils.setField(user, "id", hostId);

        when(userRepository.findByIdAndDeletedAtIsNull(hostId))
                .thenReturn(Optional.of(user));

        // when & then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> reservationService.getHostReservations(hostId, pageable)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.HOST_REQUIRED);
    }
}
