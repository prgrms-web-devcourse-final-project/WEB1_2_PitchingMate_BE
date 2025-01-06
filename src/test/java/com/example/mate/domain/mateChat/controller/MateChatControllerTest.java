package com.example.mate.domain.mateChat.controller;

import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.service.MateChatRoomService;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MateChatRoomController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember
public class MateChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MateChatRoomService chatRoomService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    @Test
    @DisplayName("메이트 게시글 채팅방 입장/생성 성공 - 최초로 입장하는 멤버라면 채팅방을 생성한다.")
    void createOrJoinChatRoomFromPost() throws Exception {
        // given
        MateChatRoomResponse response = createMockChatRoomResponse();
        given(chatRoomService.createOrJoinChatRoomFromPost(anyLong(), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/mates/chat/post/{matePostId}/join", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roomId").value(response.getRoomId()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verify(chatRoomService).createOrJoinChatRoomFromPost(anyLong(), anyLong());
    }

    @Test
    @DisplayName("메이트 채팅방 목록 조회 성공")
    void getMyChatRooms() throws Exception {
        // given
        PageResponse<MateChatRoomListResponse> pageResponse = createMockChatRoomListResponse();
        given(chatRoomService.getMyChatRooms(anyLong(), any(Pageable.class)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/mates/chat/me")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].roomId").value(1L))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verify(chatRoomService).getMyChatRooms(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("메이트 채팅 목록 페이지에서 채팅방 입장 성공")
    void joinExistingChatRoom() throws Exception {
        // given
        MateChatRoomResponse response = createMockChatRoomResponse();
        given(chatRoomService.joinExistingChatRoom(anyLong(), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/mates/chat/{chatroomId}/join", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roomId").value(response.getRoomId()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verify(chatRoomService).joinExistingChatRoom(anyLong(), anyLong());
    }

    @Test
    @DisplayName("메이트 채팅방 메시지 조회 성공")
    void getChatMessages() throws Exception {
        // given
        PageResponse<MateChatMessageResponse> pageResponse = createMockChatMessagesResponse();
        given(chatRoomService.getChatMessages(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/mates/chat/{chatroomId}/messages", 1L)
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].messageId").value(1L))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verify(chatRoomService).getChatMessages(anyLong(), anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("메이트 채팅방 멤버 조회 성공")
    void getMateChatRoomMembers() throws Exception {
        // given
        List<MemberSummaryResponse> members = createMockMemberResponses();
        given(chatRoomService.getChatRoomMembers(anyLong(), anyLong()))
                .willReturn(members);

        // when & then
        mockMvc.perform(get("/api/mates/chat/{chatRoomId}/members", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].memberId").value(1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verify(chatRoomService).getChatRoomMembers(anyLong(), anyLong());
    }

    private MateChatRoomResponse createMockChatRoomResponse() {
        return MateChatRoomResponse.builder()
                .roomId(1L)
                .matePostId(1L)
                .memberId(1L)
                .currentMembers(2)
                .isRoomActive(true)
                .isMessageable(true)
                .isAuthorLeft(false)
                .isAuthor(false)
                .initialMessages(createMockChatMessagesResponse())
                .build();
    }

    private PageResponse<MateChatRoomListResponse> createMockChatRoomListResponse() {
        MateChatRoomListResponse roomList = MateChatRoomListResponse.builder()
                .roomId(1L)
                .postId(1L)
                .postImageUrl("image.jpg")
                .postTitle("Test Post")
                .lastMessageContent("Last message")
                .lastMessageTime(LocalDateTime.now())
                .currentMembers(2)
                .isActive(true)
                .isMessageable(true)
                .isAuthorLeft(false)
                .isAuthor(false)
                .build();

        return PageResponse.<MateChatRoomListResponse>builder()
                .content(List.of(roomList))
                .totalPages(1)
                .totalElements(1)
                .hasNext(false)
                .pageNumber(0)
                .pageSize(10)
                .build();
    }

    private PageResponse<MateChatMessageResponse> createMockChatMessagesResponse() {
        MateChatMessageResponse message = MateChatMessageResponse.builder()
                .messageId(1L)
                .roomId(1L)
                .senderId(1L)
                .senderNickname("Test User")
                .message("Test Message")
                .messageType("TALK")
                .senderImageUrl("image.jpg")
                .sendTime(LocalDateTime.now())
                .build();

        return PageResponse.<MateChatMessageResponse>builder()
                .content(List.of(message))
                .totalPages(1)
                .totalElements(1)
                .hasNext(false)
                .pageNumber(0)
                .pageSize(20)
                .build();
    }

    private List<MemberSummaryResponse> createMockMemberResponses() {
        MemberSummaryResponse member = MemberSummaryResponse.builder()
                .memberId(1L)
                .nickname("Test User")
                .imageUrl("image.jpg")
                .build();

        return List.of(member);
    }
}
