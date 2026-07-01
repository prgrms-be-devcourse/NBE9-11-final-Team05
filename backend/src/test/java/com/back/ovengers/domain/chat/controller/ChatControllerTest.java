package com.back.ovengers.domain.chat.controller;

import com.back.ovengers.domain.chat.entity.ChatRoom;
import com.back.ovengers.domain.chat.entity.ChatRoomMember;
import com.back.ovengers.domain.chat.enums.ChatRoomStatus;
import com.back.ovengers.domain.chat.enums.ChatRoomType;
import com.back.ovengers.domain.chat.repository.ChatRoomMemberRepository;
import com.back.ovengers.domain.chat.repository.ChatRoomRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtProvider jwtProvider;
    @Autowired UserRepository userRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired ChatRoomMemberRepository chatRoomMemberRepository;

    User user;
    Cookie accessTokenCookie;
    ChatRoom openRoom;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("chat@test.com")
                .password("password123!")
                .name("채팅유저")
                .nickname("채팅닉")
                .phone("010-1111-2222")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        String token = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        accessTokenCookie = new Cookie("accessToken", token);

        openRoom = chatRoomRepository.save(ChatRoom.builder()
                .name("테스트 오픈채팅방")
                .type(ChatRoomType.OPEN)
                .status(ChatRoomStatus.ACTIVE)
                .campingId(1L)
                .build());

        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .roomId(openRoom.getId())
                .userId(user.getId())
                .build());
    }

    @Test
    @DisplayName("채팅방 목록 조회 성공")
    void t1() throws Exception {
        mockMvc.perform(get("/api/chats/rooms")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채팅방 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("채팅방 목록 조회 실패 - 미로그인")
    void t2() throws Exception {
        mockMvc.perform(get("/api/chats/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("채팅 메시지 조회 성공 - 빈 목록")
    void t3() throws Exception {
        mockMvc.perform(get("/api/chats/rooms/{roomId}/messages", openRoom.getId())
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("메시지 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("채팅 메시지 조회 실패 - 미로그인")
    void t4() throws Exception {
        mockMvc.perform(get("/api/chats/rooms/{roomId}/messages", openRoom.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("채팅 메시지 조회 실패 - 잘못된 roomId")
    void t5() throws Exception {
        mockMvc.perform(get("/api/chats/rooms/{roomId}/messages", 0L)
                        .cookie(accessTokenCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅 메시지 조회 실패 - 채팅방 없음")
    void t6() throws Exception {
        mockMvc.perform(get("/api/chats/rooms/{roomId}/messages", 99999L)
                        .cookie(accessTokenCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("오픈채팅방 입장 성공 - 이미 멤버면 기존 roomId 반환")
    void t7() throws Exception {
        mockMvc.perform(post("/api/chats/campings/{campingId}/join", 1L)
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채팅 참여 성공"))
                .andExpect(jsonPath("$.data.roomId").value(openRoom.getId()));
    }

    @Test
    @DisplayName("오픈채팅방 입장 실패 - 미로그인")
    void t8() throws Exception {
        mockMvc.perform(post("/api/chats/campings/{campingId}/join", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("오픈채팅방 입장 실패 - 채팅방 없는 캠핑장")
    void t9() throws Exception {
        mockMvc.perform(post("/api/chats/campings/{campingId}/join", 99999L)
                        .cookie(accessTokenCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("1:1 채팅방 조회 실패 - 미로그인")
    void t10() throws Exception {
        mockMvc.perform(get("/api/chats/direct")
                        .param("reservationId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("1:1 채팅방 조회 실패 - 채팅방 없음")
    void t11() throws Exception {
        mockMvc.perform(get("/api/chats/direct")
                        .param("reservationId", "99999")
                        .cookie(accessTokenCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("1:1 채팅방 조회 실패 - reservationId 0 이하")
    void t12() throws Exception {
        mockMvc.perform(get("/api/chats/direct")
                        .param("reservationId", "0")
                        .cookie(accessTokenCookie))
                .andExpect(status().isBadRequest());
    }
}
