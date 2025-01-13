package com.example.mate.domain.goodsChat.controller;

import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomSummaryResponse;
import com.example.mate.domain.goodsChat.service.GoodsChatService;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GoodsChatRoomController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember
class GoodsChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoodsChatService goodsChatService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    @Test
    @DisplayName("굿즈거래 채팅방 생성 성공 - 기존 채팅방이 있을 경우 해당 채팅방을 반환한다.")
    void returnExistingChatRoom() throws Exception {
        // given
        Long buyerId = 1L;
        Long goodsPostId = 1L;
        GoodsChatRoomResponse existingChatRoomResponse = GoodsChatRoomResponse.builder()
                .chatRoomId(1L)
                .goodsPostId(goodsPostId)
                .teamName("test team")
                .title("test title")
                .category("ACCESSORY")
                .price(10000)
                .postStatus("OPEN")
                .imageUrl("/images/test.jpg")
                .build();

        when(goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId))
                .thenReturn(existingChatRoomResponse);

        // when & then
        mockMvc.perform(post("/api/goods/chat", buyerId)
                        .param("buyerId", String.valueOf(buyerId))
                        .param("goodsPostId", String.valueOf(goodsPostId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatRoomId").value(existingChatRoomResponse.getChatRoomId()))
                .andExpect(jsonPath("$.data.goodsPostId").value(existingChatRoomResponse.getGoodsPostId()))
                .andExpect(jsonPath("$.data.teamName").value(existingChatRoomResponse.getTeamName()))
                .andExpect(jsonPath("$.data.title").value(existingChatRoomResponse.getTitle()))
                .andExpect(jsonPath("$.data.category").value(existingChatRoomResponse.getCategory()))
                .andExpect(jsonPath("$.data.price").value(existingChatRoomResponse.getPrice()))
                .andExpect(jsonPath("$.data.postStatus").value(existingChatRoomResponse.getPostStatus()))
                .andExpect(jsonPath("$.data.imageUrl").value(existingChatRoomResponse.getImageUrl()));


        verify(goodsChatService).getOrCreateGoodsChatRoom(buyerId, goodsPostId);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 생성 성공 - 기존 채팅방이 없을 경우 새로운 채팅방을 생성한다.")
    void createNewChatRoomIfNoneExists() throws Exception {
        // given
        Long buyerId = 1L;
        Long goodsPostId = 1L;
        GoodsChatRoomResponse newChatRoomResponse = GoodsChatRoomResponse.builder()
                .chatRoomId(2L)
                .goodsPostId(goodsPostId)
                .teamName("test team")
                .title("test title")
                .category("ACCESSORY")
                .price(10000)
                .postStatus("OPEN")
                .imageUrl("/images/test.jpg")
                .build();

        when(goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId))
                .thenReturn(newChatRoomResponse);

        // when & then
        mockMvc.perform(post("/api/goods/chat", buyerId)
                        .param("goodsPostId", String.valueOf(goodsPostId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatRoomId").value(newChatRoomResponse.getChatRoomId()))
                .andExpect(jsonPath("$.data.goodsPostId").value(newChatRoomResponse.getGoodsPostId()))
                .andExpect(jsonPath("$.data.teamName").value(newChatRoomResponse.getTeamName()))
                .andExpect(jsonPath("$.data.title").value(newChatRoomResponse.getTitle()))
                .andExpect(jsonPath("$.data.category").value(newChatRoomResponse.getCategory()))
                .andExpect(jsonPath("$.data.price").value(newChatRoomResponse.getPrice()))
                .andExpect(jsonPath("$.data.postStatus").value(newChatRoomResponse.getPostStatus()))
                .andExpect(jsonPath("$.data.imageUrl").value(newChatRoomResponse.getImageUrl()));

        verify(goodsChatService).getOrCreateGoodsChatRoom(buyerId, goodsPostId);
    }

    @Test
    @DisplayName("채팅 내역 조회 성공 - 회원이 채팅방에 참여한 경우 메시지를 페이지로 반환한다.")
    void getMessagesForChatRoom_should_return_messages() throws Exception {
        // given
        Long chatRoomId = 1L;
        Long memberId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        GoodsChatMessageResponse firstMessage = GoodsChatMessageResponse.builder()
                .chatMessageId("1")
                .message("first message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .build();

        GoodsChatMessageResponse secondMessage = GoodsChatMessageResponse.builder()
                .chatMessageId("2")
                .message("second message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now())
                .build();

        PageResponse<GoodsChatMessageResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(secondMessage, firstMessage), pageable, 2),
                List.of(secondMessage, firstMessage)
        );

        when(goodsChatService.getChatRoomMessages(chatRoomId, memberId, pageable)).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/goods/chat/{chatRoomId}/message", chatRoomId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].message").value(secondMessage.getMessage()))
                .andExpect(jsonPath("$.data.content[0].chatMessageId").value(secondMessage.getChatMessageId()))
                .andExpect(jsonPath("$.data.content[1].message").value(firstMessage.getMessage()))
                .andExpect(jsonPath("$.data.content[1].chatMessageId").value(firstMessage.getChatMessageId()));

        verify(goodsChatService).getChatRoomMessages(chatRoomId, memberId, pageable);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 상세 조회 성공")
    void getGoodsChatRoomInfo_should_return_chatroom_info_and_latest_message() throws Exception {
        // given
        Long chatRoomId = 1L;
        Long memberId = 1L;
        Long goodsPostId = 1L;

        GoodsChatMessageResponse firstMessage = GoodsChatMessageResponse.builder()
                .chatMessageId("1")
                .message("first message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .build();

        GoodsChatMessageResponse secondMessage = GoodsChatMessageResponse.builder()
                .chatMessageId("2")
                .message("second message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now())
                .build();

        List<GoodsChatMessageResponse> message = List.of(secondMessage, firstMessage);

        GoodsChatRoomResponse existingChatRoomResponse = GoodsChatRoomResponse.builder()
                .chatRoomId(1L)
                .goodsPostId(goodsPostId)
                .teamName("test team")
                .title("test title")
                .category("ACCESSORY")
                .price(10000)
                .postStatus("OPEN")
                .imageUrl("/images/test.jpg")
                .initialMessages(PageResponse.from(new PageImpl<>(List.of(message)), message))
                .build();

        when(goodsChatService.getGoodsChatRoomInfo(memberId, chatRoomId)).thenReturn(existingChatRoomResponse);

        mockMvc.perform(get("/api/goods/chat/{chatRoomId}", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatRoomId").value(chatRoomId))
                .andExpect(jsonPath("$.data.goodsPostId").value(goodsPostId))
                .andExpect(jsonPath("$.data.initialMessages.content").isArray())
                .andExpect(jsonPath("$.data.initialMessages.content[0].chatMessageId").value(secondMessage.getChatMessageId()))
                .andExpect(jsonPath("$.data.initialMessages.content[0].message").value(secondMessage.getMessage()))
                .andExpect(jsonPath("$.data.initialMessages.content[1].chatMessageId").value(firstMessage.getChatMessageId()))
                .andExpect(jsonPath("$.data.initialMessages.content[1].message").value(firstMessage.getMessage()));
    }

    @Test
    @DisplayName("굿즈거래 채팅방 목록 조회 성공")
    void getGoodsChatRooms_should_return_chat_room_list() throws Exception {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        GoodsChatRoomSummaryResponse chatRoom = GoodsChatRoomSummaryResponse.builder()
                .chatRoomId(1L)
                .opponentNickname("Opponent1")
                .lastChatContent("First message")
                .lastChatSentAt(LocalDateTime.now().minusMinutes(10))
                .placeName("Test Place")
                .goodsMainImageUrl("/images/goods1.jpg")
                .opponentImageUrl("/images/opponent1.jpg")
                .build();

        PageResponse<GoodsChatRoomSummaryResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(chatRoom), pageable, 1), List.of(chatRoom));

        when(goodsChatService.getGoodsChatRooms(memberId, pageable)).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/goods/chat")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].chatRoomId").value(chatRoom.getChatRoomId()))
                .andExpect(jsonPath("$.data.content[0].opponentNickname").value(chatRoom.getOpponentNickname()))
                .andExpect(jsonPath("$.data.content[0].lastChatContent").value(chatRoom.getLastChatContent()))
                .andExpect(jsonPath("$.data.content[0].lastChatSentAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].placeName").value(chatRoom.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].goodsMainImageUrl").value(chatRoom.getGoodsMainImageUrl()))
                .andExpect(jsonPath("$.data.content[0].opponentImageUrl").value(chatRoom.getOpponentImageUrl()));

        verify(goodsChatService).getGoodsChatRooms(memberId, pageable);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 메시지 조회 성공")
    void getGoodsChatRoomMessages_should_return_messages() throws Exception {
        // given
        Long chatRoomId = 1L;
        Long memberId = 1L;
        PageRequest pageable = PageRequest.of(0, 20);

        GoodsChatMessageResponse firstMessage = GoodsChatMessageResponse.builder()
                .chatMessageId("1")
                .message("First message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .build();

        GoodsChatMessageResponse secondMessage = GoodsChatMessageResponse.builder()
                .chatMessageId("2")
                .message("Second message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now())
                .build();

        PageResponse<GoodsChatMessageResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(secondMessage, firstMessage), pageable, 2),
                List.of(secondMessage, firstMessage)
        );

        when(goodsChatService.getChatRoomMessages(chatRoomId, memberId, pageable)).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/goods/chat/{chatRoomId}/message", chatRoomId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].chatMessageId").value(secondMessage.getChatMessageId()))
                .andExpect(jsonPath("$.data.content[0].message").value(secondMessage.getMessage()))
                .andExpect(jsonPath("$.data.content[1].chatMessageId").value(firstMessage.getChatMessageId()))
                .andExpect(jsonPath("$.data.content[1].message").value(firstMessage.getMessage()));

        verify(goodsChatService).getChatRoomMessages(chatRoomId, memberId, pageable);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 나가기 성공")
    void leaveGoodsChatRoom_should_deactivate_chat_part_and_return_no_content() throws Exception {
        // given
        Long memberId = 1L;
        Long chatRoomId = 1L;

        willDoNothing().given(goodsChatService).deactivateGoodsChatPart(memberId, chatRoomId);

        // when & then
        mockMvc.perform(delete("/api/goods/chat/{chatRoomId}", chatRoomId))
                .andExpect(status().isNoContent());

        verify(goodsChatService).deactivateGoodsChatPart(memberId, chatRoomId);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 참여자 목록 조회 성공")
    void getGoodsChatRoomMembers_should_return_list_of_chat_members() throws Exception {
        // given
        Long memberId = 1L;
        Long chatRoomId = 1L;

        MemberSummaryResponse member = MemberSummaryResponse.builder()
                .memberId(memberId)
                .nickname("member1")
                .imageUrl("/images/member1.jpg")
                .build();

        MemberSummaryResponse anotherMember = MemberSummaryResponse.builder()
                .memberId(2L)
                .nickname("member2")
                .imageUrl("/images/member2.jpg")
                .build();

        List<MemberSummaryResponse> memberList = List.of(member, anotherMember);

        when(goodsChatService.getMembersInChatRoom(memberId, chatRoomId)).thenReturn(memberList);

        // when & then
        mockMvc.perform(get("/api/goods/chat/{chatRoomId}/members", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].memberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.data[0].nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data[0].imageUrl").value(member.getImageUrl()))
                .andExpect(jsonPath("$.data[1].memberId").value(anotherMember.getMemberId()))
                .andExpect(jsonPath("$.data[1].nickname").value(anotherMember.getNickname()))
                .andExpect(jsonPath("$.data[1].imageUrl").value(anotherMember.getImageUrl()));

        verify(goodsChatService).getMembersInChatRoom(memberId, chatRoomId);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 참여자 목록 조회 실패 - 참여하지 않은 사용자가 조회할 경우 예외 발생")
    void getGoodsChatRoomMembers_should_throw_exception_when_user_is_not_a_member() throws Exception {
        // given
        Long memberId = 1L;
        Long chatRoomId = 2L;

        when(goodsChatService.getMembersInChatRoom(memberId, chatRoomId))
                .thenThrow(new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART));

        // when & then
        mockMvc.perform(get("/api/goods/chat/{chatRoomId}/members", chatRoomId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getMessage()));

        verify(goodsChatService).getMembersInChatRoom(memberId, chatRoomId);
    }
}