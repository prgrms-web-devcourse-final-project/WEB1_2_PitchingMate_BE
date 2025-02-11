package com.example.mate.domain.goodsPost.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.goodsPost.dto.request.GoodsPostRequest;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostResponse;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goodsPost.dto.response.LocationInfo;
import com.example.mate.domain.goodsPost.dto.response.MemberInfo;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostImageRepository;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class GoodsPostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GoodsPostRepository goodsPostRepository;
    @Autowired
    private GoodsPostImageRepository imageRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private GoodsPost goodsPost;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        member = createMember();
        goodsPost = createGoodsPost(Status.OPEN, null);
        createGoodsPostImage();
    }

    @Test
    @DisplayName("굿즈거래 판매글 작성 통합 테스트")
    @WithAuthMember
    void register_goods_post_integration_test() throws Exception {
        // given
        Long memberId = member.getId();
        LocationInfo locationInfo = createLocationInfo();
        GoodsPostRequest goodsPostRequest = new GoodsPostRequest(1L, "title", Category.ACCESSORY, 10_000, "content",
                locationInfo);
        List<MockMultipartFile> files = List.of(createFile(), createFile());

        MockMultipartFile data = new MockMultipartFile(
                "data", "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(goodsPostRequest)
        );

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest = multipart("/api/goods").file(data);
        files.forEach(multipartRequest::file);

        MockHttpServletResponse result = mockMvc.perform(multipartRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<GoodsPostResponse> apiResponse = objectMapper.readValue(result.getContentAsString(),
                new TypeReference<>() {
                });

        // then
        assertApiResponse(apiResponse, goodsPostRequest, files);
        assertActualData(apiResponse.getData().getId(), goodsPostRequest, files);
        assertThat(member.getManner()).isCloseTo(0.301f, within(0.0001f));
    }

    @Test
    @DisplayName("굿즈거래 판매글 수정 통합 테스트")
    @WithAuthMember
    void update_goods_post_integration_test() throws Exception {
        // given
        Long memberId = member.getId();
        Long goodsPostId = goodsPost.getId();
        LocationInfo locationInfo = createLocationInfo();
        GoodsPostRequest goodsPostRequest = new GoodsPostRequest(1L, "update tile", Category.CAP, 10_000,
                "update content", locationInfo);
        List<MockMultipartFile> files = List.of(createFile(), createFile(), createFile());

        MockMultipartFile data = new MockMultipartFile(
                "data", "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(goodsPostRequest)
        );

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest = multipart("/api/goods/{goodsPostId}",
                goodsPostId).file(data);
        files.forEach(multipartRequest::file);
        multipartRequest.with(request -> {
            request.setMethod("PUT"); // PUT 메서드로 변경
            return request;
        });

        MockHttpServletResponse result = mockMvc.perform(multipartRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<GoodsPostResponse> apiResponse = objectMapper.readValue(result.getContentAsString(),
                new TypeReference<>() {
                });

        // then
        assertApiResponse(apiResponse, goodsPostRequest, files);
        assertActualData(apiResponse.getData().getId(), goodsPostRequest, files);
    }

    // ApiResponse 검증
    private void assertApiResponse(ApiResponse<GoodsPostResponse> apiResponse, GoodsPostRequest expected,
                                   List<MockMultipartFile> files) {
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo("SUCCESS");

        GoodsPostResponse response = apiResponse.getData();
        assertThat(response.getCategory()).isEqualTo(expected.getCategory().getValue());
        assertThat(response.getContent()).isEqualTo(expected.getContent());
        assertThat(response.getPrice()).isEqualTo(expected.getPrice());
        assertThat(response.getTitle()).isEqualTo(expected.getTitle());
        assertThat(response.getStatus()).isEqualTo("거래중");
        assertThat(response.getLocation()).isEqualTo(expected.getLocation());

        MemberInfo memberInfo = response.getSeller();
        assertThat(memberInfo.getMemberId()).isEqualTo(member.getId());
        assertThat(memberInfo.getManner()).isEqualTo(member.getManner());
        assertThat(memberInfo.getNickname()).isEqualTo(member.getNickname());

        List<String> imageUrls = response.getImageUrls();
        assertThat(imageUrls).isNotEmpty();
        assertThat(imageUrls.size()).isEqualTo(files.size());
    }

    // 저장된 DB 데이터 검증
    private void assertActualData(Long postId, GoodsPostRequest expected, List<MockMultipartFile> files) {
        GoodsPost goodsPost = goodsPostRepository.findById(postId).get();

        assertThat(goodsPost.getCategory()).isEqualTo(expected.getCategory());
        assertThat(goodsPost.getContent()).isEqualTo(expected.getContent());
        assertThat(goodsPost.getPrice()).isEqualTo(expected.getPrice());
        assertThat(goodsPost.getTitle()).isEqualTo(expected.getTitle());
        assertThat(goodsPost.getStatus()).isEqualTo(Status.OPEN);

        List<GoodsPostImage> goodsPostImages = goodsPost.getGoodsPostImages();
        assertThat(goodsPostImages).isNotEmpty();
        assertThat(goodsPostImages).hasSize(files.size());
    }

    @Test
    @DisplayName("굿즈거래 판매글 삭제 통합 테스트")
    @WithAuthMember
    void delete_goods_post_integration_test() throws Exception {
        // given
        Long memberId = member.getId();
        Long goodsPostId = goodsPost.getId();

        // when
        mockMvc.perform(delete("/api/goods/{goodsPostId}", goodsPostId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // then
        Optional<GoodsPost> goodsPost = goodsPostRepository.findById(goodsPostId);
        assertThat(goodsPost).isEmpty();
        assertThat(member.getManner()).isCloseTo(0.299f, within(0.0001f));
    }

    @Test
    @DisplayName("굿즈거래 판매글 상세 조회 통합 테스트")
    void get_goods_post_integration_test() throws Exception {
        // given
        Long goodsPostId = goodsPost.getId();

        // when
        MockHttpServletResponse result = mockMvc.perform(get("/api/goods/{goodsPostId}", goodsPostId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<GoodsPostResponse> apiResponse = objectMapper.readValue(result.getContentAsString(),
                new TypeReference<>() {
                });

        // then
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo("SUCCESS");

        GoodsPostResponse response = apiResponse.getData();
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(goodsPostId);
        assertThat(response.getTitle()).isEqualTo(goodsPost.getTitle());
        assertThat(response.getContent()).isEqualTo(goodsPost.getContent());
        assertThat(response.getPrice()).isEqualTo(goodsPost.getPrice());
        assertThat(response.getCategory()).isEqualTo(goodsPost.getCategory().getValue());
        assertThat(response.getLocation().getPlaceName()).isEqualTo(goodsPost.getLocation().getPlaceName());

        MemberInfo seller = response.getSeller();
        assertThat(seller.getMemberId()).isEqualTo(member.getId());
        assertThat(seller.getNickname()).isEqualTo(member.getNickname());
        assertThat(seller.getManner()).isEqualTo(member.getManner());
    }

    @Test
    @DisplayName("메인페이지 굿즈 판매글 리스트 조회 통합 테스트")
    void get_main_goods_posts_integration_test() throws Exception {
        // given
        Long teamId = 1L;

        // when
        MockHttpServletResponse result = mockMvc.perform(get("/api/goods/main")
                        .param("teamId", String.valueOf(teamId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<List<GoodsPostSummaryResponse>> apiResponse = objectMapper.readValue(result.getContentAsString(),
                new TypeReference<>() {
                });

        // then
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo("SUCCESS");

        List<GoodsPostSummaryResponse> goodsPostResponses = apiResponse.getData();
        assertThat(goodsPostResponses).hasSize(1);

        GoodsPostSummaryResponse response = goodsPostResponses.get(0);

        assertThat(response.getTitle()).isEqualTo(goodsPost.getTitle());
        assertThat(response.getTeamName()).isEqualTo(TeamInfo.getById(goodsPost.getTeamId()).shortName);
        assertThat(response.getPrice()).isEqualTo(goodsPost.getPrice());
        assertThat(response.getCategory()).isEqualTo(goodsPost.getCategory().getValue());
        assertThat(response.getImageUrl()).isEqualTo(
                FileUtils.getThumbnailImageUrl(goodsPost.getGoodsPostImages().get(0).getImageUrl()));
    }

    @Test
    @DisplayName("메인페이지 굿즈 판매글 페이징 조회 통합 테스트")
    void get_main_goods_posts_paging_integration_test() throws Exception {
        // given
        Long teamId = 10L;
        int page = 0;
        int size = 10;

        goodsPostRepository.deleteAll();
        imageRepository.deleteAll();

        for (int i = 1; i <= 3; i++) {
            GoodsPost post = GoodsPost.builder()
                    .seller(member)
                    .teamId(10L)
                    .title("Test Title " + i)
                    .content("Test Content " + i)
                    .price(1000 * i)
                    .category(Category.ACCESSORY)
                    .location(LocationInfo.toEntity(createLocationInfo()))
                    .build();

            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl("upload/test_img_url " + i)
                    .post(post)
                    .build();

            post.changeImages(List.of(image));
            goodsPostRepository.save(post);
        }

        // when
        MockHttpServletResponse result = mockMvc.perform(get("/api/goods/main")
                        .param("teamId", String.valueOf(teamId))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<List<GoodsPostSummaryResponse>> apiResponse = objectMapper.readValue(result.getContentAsString(),
                new TypeReference<>() {
                });

        // then
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo("SUCCESS");

        List<GoodsPostSummaryResponse> goodsPostResponses = apiResponse.getData();
        assertThat(goodsPostResponses).hasSize(3);

        GoodsPostSummaryResponse firstResponse = goodsPostResponses.get(0);
        assertThat(firstResponse.getTitle()).isEqualTo("Test Title 3");
        assertThat(firstResponse.getPrice()).isEqualTo(3_000);
        assertThat(firstResponse.getCategory()).isEqualTo(Category.ACCESSORY.getValue());
        assertThat(firstResponse.getImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl("upload/test_img_url 3"));
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

    private GoodsPost createGoodsPost(Status status, Member buyer) {
        return goodsPostRepository.save(GoodsPost.builder()
                .seller(member)
                .teamId(1L)
                .buyer(buyer)
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

    private MockMultipartFile createFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("test_photo.png");

        return new MockMultipartFile(
                "files",
                "test_photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                Files.readAllBytes(resource.getFile().toPath())
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
