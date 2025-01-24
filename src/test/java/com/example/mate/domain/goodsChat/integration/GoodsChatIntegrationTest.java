package com.example.mate.domain.goodsChat.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.config.mongoConfig.AcceptanceTestWithMongo;
import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatPartRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.goodsPost.dto.response.LocationInfo;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsPost.entity.Role;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class GoodsChatIntegrationTest extends AcceptanceTestWithMongo {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private GoodsPostRepository goodsPostRepository;
    @Autowired private GoodsChatRoomRepository chatRoomRepository;
    @Autowired private GoodsChatPartRepository chatPartRepository;
    @Autowired private GoodsChatMessageRepository messageRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;


    private Member seller;
    private Member buyer;
    private GoodsPost goodsPost;
    private GoodsChatRoom chatRoom;
    private List<GoodsChatMessage> messages = new ArrayList<>();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

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
    @WithAuthMember(memberId = 3L)
    void get_or_create_goods_chatroom_integration_test() throws Exception {
        // given
        Member buyer = createMember("test buyer", "test buyer nickname", "test-buyer@gmail.com");
        Long buyerId = buyer.getId();
        Long goodsPostId = goodsPost.getId();

        // when
        MockHttpServletResponse result = mockMvc.perform(post("/api/goods/chat", buyerId)
                        .param("goodsPostId", String.valueOf(goodsPostId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.goodsPostId").value(goodsPost.getId()))
                .andExpect(jsonPath("$.data.title").value(goodsPost.getTitle()))
                .andExpect(jsonPath("$.data.category").value(goodsPost.getCategory().getValue()))
                .andExpect(jsonPath("$.data.price").value(goodsPost.getPrice()))
                .andExpect(jsonPath("$.data.postStatus").value(goodsPost.getStatus().getValue()))
                .andExpect(jsonPath("$.data.imageUrl").value(FileUtils.getThumbnailImageUrl(goodsPost.getGoodsPostImages().get(0).getImageUrl())))
                .andReturn()
                .getResponse();

        result.setCharacterEncoding("UTF-8");
        ApiResponse<GoodsChatRoomResponse> apiResponse = objectMapper.readValue(result.getContentAsString(), new TypeReference<>() {});
        GoodsChatRoomResponse actualResponse = apiResponse.getData();

        // then
        GoodsChatRoom actualChatRoom = chatRoomRepository.findById(actualResponse.getChatRoomId()).orElseThrow();
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
    @WithAuthMember(memberId = 2L)
    void get_messages_for_chat_room() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();
        Long memberId = buyer.getId();
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        mockMvc.perform(get("/api/goods/chat/{chatRoomId}/message", chatRoomId)
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].message").value("test message"))
                .andExpect(jsonPath("$.data[1].message").value("test message"))
                .andReturn()
                .getResponse();
    }

    @Test
    @DisplayName("굿즈거래 채팅방 상세 조회 테스트")
    @WithAuthMember(memberId = 2L)
    void get_goods_chat_room_info() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();
        Long memberId = buyer.getId();

        // when
        MockHttpServletResponse result = mockMvc.perform(get("/api/goods/chat/{chatRoomId}", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.initialMessages").isArray())
                .andExpect(jsonPath("$.data.initialMessages[0].message").value("test message"))
                .andExpect(jsonPath("$.data.initialMessages[1].message").value("test message"))
                .andReturn()
                .getResponse();

        result.setCharacterEncoding("UTF-8");
        ApiResponse<GoodsChatRoomResponse> apiResponse = objectMapper.readValue(result.getContentAsString(), new TypeReference<>() {});
        GoodsChatRoomResponse response = apiResponse.getData();

        // then
        GoodsChatRoom actualChatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        GoodsPost actualPost = actualChatRoom.getGoodsPost();

        assertThat(response.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.getGoodsPostId()).isEqualTo(actualPost.getId());
        assertThat(response.getTitle()).isEqualTo(actualPost.getTitle());
        assertThat(response.getPrice()).isEqualTo(actualPost.getPrice());
        assertThat(response.getPostStatus()).isEqualTo(actualPost.getStatus().getValue());

        GoodsPostImage image = actualPost.getGoodsPostImages().get(0);
        assertThat(image.getImageUrl()).isEqualTo("upload/test_img_url");
    }

    @Test
    @DisplayName("굿즈거래 채팅방 목록 조회 성공 통합 테스트")
    @WithAuthMember(memberId = 2L)
    void getGoodsChatRooms_should_return_chat_room_list_integration_test() throws Exception {
        // given
        Long memberId = buyer.getId();
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        mockMvc.perform(get("/api/goods/chat")
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].chatRoomId").value(chatRoom.getId()))
                .andExpect(jsonPath("$.data.content[0].opponentNickname").value(seller.getNickname()))
                .andExpect(jsonPath("$.data.content[0].lastChatContent").value(chatRoom.getLastChatContent()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(goodsPost.getLocation().getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].goodsMainImageUrl").value(FileUtils.getThumbnailImageUrl(goodsPost.getMainImageUrl())))
                .andExpect(jsonPath("$.data.content[0].opponentImageUrl").value(FileUtils.getThumbnailImageUrl(seller.getImageUrl())))
                .andReturn()
                .getResponse();
    }

    @Test
    @DisplayName("굿즈거래 채팅방 메시지 조회 성공 통합 테스트")
    @WithAuthMember(memberId = 2L)
    void getGoodsChatRoomMessages_integration_test() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();
        Long buyerId = buyer.getId();
        int size = 20;

        // when & then
        MockHttpServletResponse response = mockMvc.perform(get("/api/goods/chat/{chatRoomId}/message", chatRoomId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn()
                .getResponse();

        response.setCharacterEncoding("UTF-8");
        ApiResponse<List<GoodsChatMessageResponse>> apiResponse = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        List<GoodsChatMessageResponse> expectMessages = apiResponse.getData();

        // 실제 데이터 조회
        List<GoodsChatMessage> actualMessages = messageRepository.getChatMessages(chatRoomId, null, size);

        assertThat(expectMessages.size()).isEqualTo(actualMessages.size());

        for (int i = 0; i < expectMessages.size(); i++) {
            GoodsChatMessageResponse expectedMessage = expectMessages.get(i);
            GoodsChatMessage actualMessage = actualMessages.get(i);

            assertThat(expectedMessage.getChatMessageId()).isEqualTo(actualMessage.getId());
            assertThat(expectedMessage.getRoomId()).isEqualTo(actualMessage.getChatRoomId());
            assertThat(expectedMessage.getSenderId()).isEqualTo(actualMessage.getMemberId());
            assertThat(expectedMessage.getMessage()).isEqualTo(actualMessage.getContent());
            assertThat(expectedMessage.getMessageType()).isEqualTo(actualMessage.getMessageType().getValue());
            assertThat(expectedMessage.getSentAt()).isEqualToIgnoringNanos(actualMessage.getSentAt()); // 시간 비교, 나노초는 무시
        }
    }

    @Test
    @DisplayName("굿즈거래 채팅방 나가기 통합 테스트 - 채팅방에 한명이 남을 경우 채팅방과 당사자는 비활성화 된다.")
    @WithAuthMember(memberId = 2L)
    void leaveGoodsChatRoom_integration_test_single_member_left() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();
        Long memberId = buyer.getId();

        // when
        mockMvc.perform(delete("/api/goods/chat/{chatRoomId}", chatRoomId)).andExpect(status().isNoContent());

        // then
        GoodsChatRoom goodsChatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        assertThat(goodsChatRoom.isRoomActive()).isFalse();

        GoodsChatPart goodsChatPart = chatPartRepository.findById(new GoodsChatPartId(memberId, chatRoomId)).orElseThrow();
        assertThat(goodsChatPart.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("굿즈거래 채팅방 나가기 통합 테스트 - 채팅방에 아무도 남지 않을 경우 채팅방과 채팅참여, 채팅은 모두 삭제된다.")
    @WithAuthMember(memberId = 2L)
    void leaveGoodsChatRoom_integration_test_no_participants_left() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();
        Long memberId = buyer.getId();

        GoodsChatPart goodsChatPart = chatRoom.getChatParts().get(1);
        goodsChatPart.leaveAndCheckRoomStatus();

        chatRoomRepository.saveAndFlush(chatRoom);

        // when
        mockMvc.perform(delete("/api/goods/chat/{chatRoomId}", chatRoomId)).andExpect(status().isNoContent());

        // then
        Optional<GoodsChatRoom> goodsChatRoom = chatRoomRepository.findById(chatRoomId);
        assertThat(goodsChatRoom).isEmpty();

        List<GoodsChatPart> existingMember = chatPartRepository.findAllWithMemberByChatRoomId(chatRoomId);
        assertThat(existingMember).isEmpty();
    }

    @Test
    @DisplayName("굿즈거래 채팅방 참여자 목록 조회 성공 통합 테스트")
    @WithAuthMember(memberId = 2L)
    void getGoodsChatRoomMembers_integration_test() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();
        Long memberId = buyer.getId();

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/goods/chat/{chatRoomId}/members", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn()
                .getResponse();

        response.setCharacterEncoding("UTF-8");

        // then
        ApiResponse<List<MemberSummaryResponse>> apiResponse = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        List<MemberSummaryResponse> actualMembers = apiResponse.getData();

        // 실제 데이터 조회
        List<GoodsChatPart> chatParts = chatPartRepository.findAllWithMemberByChatRoomId(chatRoomId);
        List<Member> expectedMembers = chatParts.stream().map(GoodsChatPart::getMember).toList();

        assertThat(actualMembers.size()).isEqualTo(expectedMembers.size());

        for (int i = 0; i < actualMembers.size(); i++) {
            MemberSummaryResponse actualMember = actualMembers.get(i);
            Member expectedMember = expectedMembers.get(i);

            assertThat(actualMember.getMemberId()).isEqualTo(expectedMember.getId());
            assertThat(actualMember.getNickname()).isEqualTo(expectedMember.getNickname());
            assertThat(actualMember.getImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl(expectedMember.getImageUrl()));
        }
    }

    @Test
    @DisplayName("굿즈 거래완료 통합 테스트")
    @WithAuthMember
    void complete_goods_post_integration_test() throws Exception {
        // given
        Long chatRoomId = chatRoom.getId();

        // when
        MockHttpServletResponse result = mockMvc.perform(post("/api/goods/chat/{chatRoomId}/complete", chatRoomId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<Void> apiResponse = objectMapper.readValue(result.getContentAsString(), new TypeReference<>() {});

        // then
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo("SUCCESS");

        GoodsPost completedPost = chatRoomRepository.findByChatRoomId(chatRoomId).orElseThrow().getGoodsPost();
        assertThat(completedPost.getStatus()).isEqualTo(Status.CLOSED);
        assertThat(completedPost.getBuyer()).isNotNull();

        Member resultBuyer = completedPost.getBuyer();
        assertThat(resultBuyer.getId()).isEqualTo(buyer.getId());
        assertThat(resultBuyer.getName()).isEqualTo(buyer.getName());
        assertThat(resultBuyer.getEmail()).isEqualTo(buyer.getEmail());
        assertThat(resultBuyer.getNickname()).isEqualTo(buyer.getNickname());
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
                .teamId(1L)
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
                        .chatRoomId(goodsChatPart.getGoodsChatRoom().getId())
                        .memberId(goodsChatPart.getMember().getId())
                        .content(content)
                        .messageType(MessageType.TALK)
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
