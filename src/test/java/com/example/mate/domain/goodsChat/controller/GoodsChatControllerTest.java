package com.example.mate.domain.goodsChat.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.service.GoodsChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
}