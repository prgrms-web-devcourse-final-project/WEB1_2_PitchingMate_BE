package com.example.mate.domain.goodsChat.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GoodsChatIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private GoodsPostRepository goodsPostRepository;
    @Autowired private GoodsChatRoomRepository chatRoomRepository;
    @Autowired private GoodsChatMessageRepository messageRepository;
    @Autowired private ObjectMapper objectMapper;

    private Member seller;
    private Member buyer;
    private GoodsPost goodsPost;
    private GoodsChatRoom chatRoom;
    private List<GoodsChatMessage> messages = new ArrayList<>();

    @BeforeEach
    void setUp() {
        seller = createMember("seller", "seller nickname", "seller@gmail.com");
        buyer = createMember("buyer", "buyer nickname", "buyer@gmail.com");
        goodsPost = createGoodsPost(Status.OPEN, seller, buyer);
        createGoodsPostImage(goodsPost);

        chatRoom = createGoodsChatRoom(goodsPost);
        chatRoom.addChatParticipant(seller, Role.SELLER);
        chatRoom.addChatParticipant(buyer, Role.BUYER);
        chatRoomRepository.saveAndFlush(chatRoom);

        chatRoom.getChatParts().forEach(part -> {
            messages.add(createChatMessage(part, "test message"));
        });
    }

    @Test
    @DisplayName("굿즈거래 채팅방 생성 통합 테스트")
    void get_or_create_goods_chatroom_integration_test() throws Exception {
        // given
        Member buyer = createMember("test buyer", "test buyer nickname", "test-buyer@gmail.com");
        Long buyerId = buyer.getId();
        Long goodsPostId = goodsPost.getId();

        // when
        MockHttpServletResponse result = mockMvc.perform(post("/api/goods/chat", buyerId)
                        .param("buyerId", String.valueOf(buyerId))
                        .param("goodsPostId", String.valueOf(goodsPostId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.goodsPostId").value(goodsPost.getId()))
                .andExpect(jsonPath("$.data.title").value(goodsPost.getTitle()))
                .andExpect(jsonPath("$.data.category").value(goodsPost.getCategory().getValue()))
                .andExpect(jsonPath("$.data.price").value(goodsPost.getPrice()))
                .andExpect(jsonPath("$.data.status").value(goodsPost.getStatus().getValue()))
                .andExpect(jsonPath("$.data.imageUrl").value(goodsPost.getGoodsPostImages().get(0).getImageUrl()))
                .andReturn()
                .getResponse();

        result.setCharacterEncoding("UTF-8");
        ApiResponse<GoodsChatRoomResponse> apiResponse = objectMapper.readValue(result.getContentAsString(), new TypeReference<>() {});
        GoodsChatRoomResponse actualResponse = apiResponse.getData();

        // then
        GoodsChatRoom actualChatRoom = chatRoomRepository.findById(actualResponse.getChatRoomId()).orElse(null);
        GoodsPost actualPost = actualChatRoom.getGoodsPost();
        assertThat(actualPost.getId()).isEqualTo(goodsPost.getId());
        assertThat(actualPost.getContent()).isEqualTo(goodsPost.getContent());
        assertThat(actualPost.getTeamId()).isEqualTo(goodsPost.getTeamId());
        assertThat(actualPost.getTitle()).isEqualTo(goodsPost.getTitle());
        assertThat(actualPost.getPrice()).isEqualTo(goodsPost.getPrice());

        GoodsPostImage image = actualPost.getGoodsPostImages().get(0);
        assertThat(image.getImageUrl()).isEqualTo("upload/test_img_url");
    }

    @Test
    @DisplayName("굿즈거래 채팅방 채팅내역 조회 통합 테스트")
    void get_messages_for_chat_room() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();
        Long memberId = buyer.getId();
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        mockMvc.perform(get("/api/goods/chat/{chatRoomId}/message", chatRoomId)
                        .param("memberId", String.valueOf(memberId))
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].content").value("test message"))
                .andExpect(jsonPath("$.data.content[1].content").value("test message"))
                .andReturn()
                .getResponse();
    }

    private Member createMember(String name, String nickname, String email) {
        return memberRepository.save(Member.builder()
                .name(name)
                .nickname(nickname)
                .email(email)
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
    }

    private GoodsPost createGoodsPost(Status status, Member seller, Member buyer) {
        return goodsPostRepository.save(GoodsPost.builder()
                .teamId(1L)
                .seller(seller)
                .buyer(buyer)
                .title("test title")
                .content("test content")
                .price(10_000)
                .status(status)
                .category(Category.ACCESSORY)
                .location(LocationInfo.toEntity(createLocationInfo()))
                .build());
    }

    private GoodsChatRoom createGoodsChatRoom(GoodsPost goodsPost) {
        return chatRoomRepository.save(GoodsChatRoom.builder()
                .goodsPost(goodsPost)
                .build());
    }

    private void createGoodsPostImage(GoodsPost goodsPost) {
        GoodsPostImage image = GoodsPostImage.builder()
                .imageUrl("upload/test_img_url")
                .build();

        goodsPost.changeImages(List.of(image));
        goodsPostRepository.save(goodsPost);
    }

    private GoodsChatMessage createChatMessage(GoodsChatPart goodsChatPart, String content) {
        return messageRepository.save(
                GoodsChatMessage.builder()
                        .goodsChatPart(goodsChatPart)
                        .content(content)
                        .sentAt(LocalDateTime.now())
                        .build()
        );
    }

    private LocationInfo createLocationInfo() {
        return LocationInfo.builder()
                .placeName("Stadium Plaza")
                .longitude("127.12345")
                .latitude("37.56789")
                .build();
    }
}
