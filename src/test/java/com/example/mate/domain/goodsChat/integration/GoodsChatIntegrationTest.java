package com.example.mate.domain.goodsChat.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class GoodsChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GoodsPostRepository goodsPostRepository;
    @Autowired
    private GoodsChatRoomRepository chatRoomRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private GoodsPost goodsPost;

    @BeforeEach
    void setUp() {
        member = createMember("tester", "tester nickname", "test@gmail.com");
        goodsPost = createGoodsPost(Status.OPEN, member, null);
        createGoodsPostImage(goodsPost);
    }

    @Test
    @DisplayName("굿즈거래 채팅방 생성 통합 테스트")
    void get_or_create_goods_chatroom_integration_test() throws Exception {
        // given
        Member buyer = createMember("test buyer", "test buyer nickname", "buyer@gmail.com");
        Long buyerId = buyer.getId();
        Long goodsPostId = goodsPost.getId();

        // when
        MockHttpServletResponse result = mockMvc.perform(post("/api/goods/chat/{buyerId}", buyerId)
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
        ApiResponse<GoodsChatRoomResponse> apiResponse = objectMapper.readValue(result.getContentAsString(),
                new TypeReference<>() {
                });
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

    private LocationInfo createLocationInfo() {
        return LocationInfo.builder()
                .placeName("Stadium Plaza")
                .longitude("127.12345")
                .latitude("37.56789")
                .build();
    }
}
