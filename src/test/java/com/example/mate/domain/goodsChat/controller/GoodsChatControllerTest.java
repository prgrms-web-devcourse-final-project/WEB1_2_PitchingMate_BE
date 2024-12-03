package com.example.mate.domain.goodsChat.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMsgResponse;
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

@WebMvcTest(GoodsChatController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class GoodsChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoodsChatService goodsChatService;

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
                .status("OPEN")
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
                .andExpect(jsonPath("$.data.status").value(existingChatRoomResponse.getStatus()))
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
                .status("OPEN")
                .imageUrl("/images/test.jpg")
                .build();

        when(goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId))
                .thenReturn(newChatRoomResponse);

        // when & then
        mockMvc.perform(post("/api/goods/chat", buyerId)
                        .param("buyerId", String.valueOf(buyerId))
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
                .andExpect(jsonPath("$.data.status").value(newChatRoomResponse.getStatus()))
                .andExpect(jsonPath("$.data.imageUrl").value(newChatRoomResponse.getImageUrl()));

        verify(goodsChatService).getOrCreateGoodsChatRoom(buyerId, goodsPostId);
    }

    @Test
    @DisplayName("채팅 내역 조회 성공 - 회원이 채팅방에 참여한 경우 메시지를 페이지로 반환한다.")
    void getMessagesForChatRoom_should_return_messages() throws Exception {
        // given
        Long chatRoomId = 1L;
        Long memberId = 2L;
        PageRequest pageable = PageRequest.of(0, 10);

        GoodsChatMsgResponse firstMessage = GoodsChatMsgResponse.builder()
                .chatMessageId(1L)
                .content("first message")
                .authorId(memberId)
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .build();

        GoodsChatMsgResponse secondMessage = GoodsChatMsgResponse.builder()
                .chatMessageId(2L)
                .content("second message")
                .authorId(memberId)
                .sentAt(LocalDateTime.now())
                .build();

        PageResponse<GoodsChatMsgResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(secondMessage, firstMessage), pageable, 2),
                List.of(secondMessage, firstMessage)
        );

        when(goodsChatService.getMessagesForChatRoom(chatRoomId, memberId, pageable)).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/goods/chat/{chatRoomId}/message", chatRoomId)
                        .param("memberId", String.valueOf(memberId))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].content").value(secondMessage.getContent()))
                .andExpect(jsonPath("$.data.content[0].chatMessageId").value(secondMessage.getChatMessageId()))
                .andExpect(jsonPath("$.data.content[1].content").value(firstMessage.getContent()))
                .andExpect(jsonPath("$.data.content[1].chatMessageId").value(firstMessage.getChatMessageId()));

        verify(goodsChatService).getMessagesForChatRoom(chatRoomId, memberId, pageable);
    }

}