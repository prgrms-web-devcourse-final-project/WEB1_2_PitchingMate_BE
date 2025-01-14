package com.example.mate.domain.goodsReview.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goodsPost.dto.response.LocationInfo;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.goodsReview.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goodsReview.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goodsReview.entity.GoodsReview;
import com.example.mate.domain.goodsReview.repository.GoodsReviewRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class GoodsReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GoodsPostRepository goodsPostRepository;
    @Autowired
    private GoodsReviewRepository reviewRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private Member seller;
    private GoodsPost goodsPost;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        member = createMember();
        seller = createSeller();
        goodsPost = createGoodsPost(Status.OPEN, null);
        createGoodsPostImage();
    }

    @Test
    @DisplayName("굿즈 거래 후기 등록 통합 테스트 - 성공")
    @WithAuthMember
    void register_goods_review_integration_test_success() throws Exception {
        // given
        Member buyer = member;
        GoodsPost completePost = createGoodsPost(Status.CLOSED, buyer);

        GoodsReviewRequest request = new GoodsReviewRequest(Rating.GREAT, "Great seller!");

        // when
        MockHttpServletResponse result = mockMvc.perform(post("/api/goods/review/{goodsPostId}", completePost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<GoodsReviewResponse> apiResponse = objectMapper.readValue(result.getContentAsString(),
                new TypeReference<>() {
                });

        // then
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo("SUCCESS");

        GoodsReviewResponse response = apiResponse.getData();
        assertThat(response).isNotNull();
        assertThat(response.getReviewId()).isNotNull();
        assertThat(response.getReviewerNickname()).isEqualTo(buyer.getNickname());
        assertThat(response.getRating()).isEqualTo(request.getRating());
        assertThat(response.getReviewContent()).isEqualTo(request.getReviewContent());
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getGoodsPostId()).isEqualTo(completePost.getId());
        assertThat(response.getGoodsPostTitle()).isEqualTo(completePost.getTitle());

        GoodsReview savedReview = reviewRepository.findById(response.getReviewId()).orElseThrow();
        assertThat(savedReview.getReviewer().getId()).isEqualTo(buyer.getId());
        assertThat(savedReview.getGoodsPost().getId()).isEqualTo(completePost.getId());
        assertThat(savedReview.getRating()).isEqualTo(Rating.GREAT);
        assertThat(savedReview.getReviewContent()).isEqualTo("Great seller!");
        assertThat(savedReview.getCreatedAt()).isNotNull();

        assertThat(seller.getManner()).isCloseTo(0.33f, within(0.0001f));
    }

    private Member createMember() {
        return memberRepository.save(Member.builder()
                .name("홍길동")
                .email("test@gmail.com")
                .nickname("테스터")
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
    }

    private Member createSeller() {
        return memberRepository.save(Member.builder()
                .name("김철수")
                .email("seller@gmail.com")
                .nickname("셀러")
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
    }

    private GoodsPost createGoodsPost(Status status, Member buyer) {
        return goodsPostRepository.save(GoodsPost.builder()
                .seller(member)
                .teamId(1L)
                .buyer(buyer)
                .seller(seller)
                .title("test title")
                .content("test content")
                .price(10_000)
                .status(status)
                .category(Category.ACCESSORY)
                .location(LocationInfo.toEntity(createLocationInfo()))
                .build());
    }

    private void createGoodsPostImage() {
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
