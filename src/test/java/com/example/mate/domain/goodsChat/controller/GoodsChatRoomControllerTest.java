package com.example.mate.domain.goodsChat.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.PageResponse;
import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.service.GoodsChatService;
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
                .chatMessageId(1L)
                .message("first message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .build();

        GoodsChatMessageResponse secondMessage = GoodsChatMessageResponse.builder()
                .chatMessageId(2L)
                .message("second message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now())
                .build();

        PageResponse<GoodsChatMessageResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(secondMessage, firstMessage), pageable, 2),
                List.of(secondMessage, firstMessage)
        );

        when(goodsChatService.getMessagesForChatRoom(chatRoomId, memberId, pageable)).thenReturn(pageResponse);

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

        verify(goodsChatService).getMessagesForChatRoom(chatRoomId, memberId, pageable);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 상세 조회 성공")
    void getGoodsChatRoomInfo_should_return_chatroom_info_and_latest_message() throws Exception {
        // given
        Long chatRoomId = 1L;
        Long memberId = 1L;
        Long goodsPostId = 1L;

        GoodsChatMessageResponse firstMessage = GoodsChatMessageResponse.builder()
                .chatMessageId(1L)
                .message("first message")
                .senderId(memberId)
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .build();

        GoodsChatMessageResponse secondMessage = GoodsChatMessageResponse.builder()
                .chatMessageId(2L)
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
}