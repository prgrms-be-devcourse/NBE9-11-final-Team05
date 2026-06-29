package com.back.ovengers.domain.camping.controller;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.service.HostCampingService;
import com.back.ovengers.domain.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HostCampingControllerTest {

    MockMvc mockMvc;

    HostCampingService hostCampingService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        hostCampingService = mock(HostCampingService.class);

        HostCampingController controller =
                new HostCampingController(hostCampingService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private RequestPostProcessor hostUser() {
        User user = mock(User.class);
        given(user.getId()).willReturn(USER_ID);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_HOST"))
                );

        return request -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return request;
        };
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("캠핑장 등록 신청 성공")
    void register_success() throws Exception {
        String requestBody = """
                {
                  "tourNum": "123-45-67890",
                  "businessNum": "111-22-33333",
                  "name": "테스트 캠핑장",
                  "region": "경기",
                  "city": "가평",
                  "address": "경기도 가평군",
                  "sites": [
                    {
                      "name": "A구역",
                      "description": "기본 구역",
                      "baseCapacity": 2,
                      "maxCapacity": 4,
                      "totalAmount": 10,
                      "price": 50000
                    }
                  ]
                }
                """;

        CampingCreateResponse response =
                new CampingCreateResponse(1L, "테스트 캠핑장", CampingStatus.PENDING);

        given(hostCampingService.register(anyLong(), any(CampingCreateRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/host/campings")
                        .with(hostUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("캠핑장 등록 신청이 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("테스트 캠핑장"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        then(hostCampingService).should()
                .register(anyLong(), any(CampingCreateRequest.class));
    }

    @Test
    @DisplayName("내 캠핑장 목록 조회 성공")
    void getMyCampings_success() throws Exception {
        List<HostCampingListResponse> response = List.of(
                new HostCampingListResponse(
                        1L,
                        "테스트 캠핑장",
                        "경기",
                        "가평",
                        "경기 가평 어쩌구",
                        "https://image.com/main.jpg",
                        4.5F,
                        CampingStatus.PENDING
                )
        );

        given(hostCampingService.getMyCampings(anyLong()))
                .willReturn(response);

        mockMvc.perform(get("/api/host/campings")
                        .with(hostUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 캠핑장 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("테스트 캠핑장"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        then(hostCampingService).should()
                .getMyCampings(anyLong());
    }

    @Test
    @DisplayName("내 캠핑장 상세 조회 성공")
    void getMyCampingDetail_success() throws Exception {
        Long campingId = 1L;

        HostCampingDetailResponse response = new HostCampingDetailResponse(
                1L,
                "https://image.com/main.jpg",
                "123-45-67890",
                "111-22-33333",
                CampingStatus.PENDING,
                "테스트 캠핑장",
                "경기",
                "가평",
                "경기도 가평군",
                "설명",
                "공지사항",
                "010-1234-5678",
                "https://camping.com",
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                BigDecimal.valueOf(37.1234567),
                BigDecimal.valueOf(127.1234567),
                4.5F,
                List.of()
        );

        given(hostCampingService.getMyCampingDetail(anyLong(), eq(campingId)))
                .willReturn(response);

        mockMvc.perform(get("/api/host/campings/{campingId}", campingId)
                        .with(hostUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 캠핑장 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("테스트 캠핑장"));

        then(hostCampingService).should()
                .getMyCampingDetail(anyLong(), eq(campingId));
    }

    @Test
    @DisplayName("캠핑장 정보 수정 성공")
    void updateCamping_success() throws Exception {
        Long campingId = 1L;

        String requestBody = """
                {
                  "name": "수정된 캠핑장",
                  "description": "수정된 설명",
                  "notice": "수정된 공지"
                }
                """;

        CampingUpdateResponse response =
                new CampingUpdateResponse(
                        1L,
                        "https://image.com/main.jpg",
                        "수정된 캠핑장",
                        "경기",
                        "가평",
                        "경기도 가평군",
                        "수정된 설명",
                        "수정된 공지",
                        "010-1234-5678",
                        "https://camping.com",
                        LocalTime.of(15, 0),
                        LocalTime.of(11, 0),
                        BigDecimal.valueOf(37.1234567),
                        BigDecimal.valueOf(127.1234567),
                        4.5F
                );

        given(hostCampingService.updateCamping(anyLong(), eq(campingId), any(CampingUpdateRequest.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/host/campings/{campingId}", campingId)
                        .with(hostUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 정보 수정이 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("수정된 캠핑장"));

        then(hostCampingService).should()
                .updateCamping(anyLong(), eq(campingId), any(CampingUpdateRequest.class));
    }

    @Test
    @DisplayName("캠핑장 삭제 성공")
    void deleteCamping_success() throws Exception {
        Long campingId = 1L;

        willDoNothing()
                .given(hostCampingService)
                .deleteCamping(anyLong(), eq(campingId));

        mockMvc.perform(delete("/api/host/campings/{campingId}", campingId)
                        .with(hostUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장이 삭제되었습니다."));

        then(hostCampingService).should()
                .deleteCamping(anyLong(), eq(campingId));
    }

    @Test
    @DisplayName("클레임 가능한 캠핑장 검색 성공")
    void searchClaimableCampings_success() throws Exception {
        String keyword = "가평";

        List<CampingClaimSearchResponse> response = List.of(
                new CampingClaimSearchResponse(
                        1L,
                        "가평 캠핑장",
                        "12345",
                        "경기",
                        "가평",
                        "경기도 가평군"
                )
        );

        given(hostCampingService.searchClaimableCampings(keyword))
                .willReturn(response);

        mockMvc.perform(get("/api/host/campings/claim/search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("클레임 가능한 캠핑장 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].name").value("가평 캠핑장"));

        then(hostCampingService).should()
                .searchClaimableCampings(keyword);
    }

    @Test
    @DisplayName("캠핑장 소유권 인증 성공")
    void claimCamping_success() throws Exception {
        String requestBody = """
                {
                  "campingId": 1,
                  "tourNum": "123-45-67890"
                }
                """;

        willDoNothing()
                .given(hostCampingService)
                .claimCamping(anyLong(), any(CampingClaimRequest.class));

        mockMvc.perform(post("/api/host/campings/claim")
                        .with(hostUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캠핑장 소유권 인증이 완료되었습니다."));

        then(hostCampingService).should()
                .claimCamping(anyLong(), any(CampingClaimRequest.class));
    }
}