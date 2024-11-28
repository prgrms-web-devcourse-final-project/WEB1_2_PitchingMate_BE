package com.example.mate.domain.goods.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.dto.MemberInfo;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostImageRepository;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GoodsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private GoodsPostRepository goodsPostRepository;
    @Autowired private GoodsPostImageRepository imageRepository;
    @Autowired private ObjectMapper objectMapper;

    private Member member;

    private GoodsPost goodsPost;

    @BeforeEach
    void setUp() {
        createMember();
        createGoodsPost();
    }

    @Test
    @DisplayName("굿즈거래 판매글 작성 통합 테스트")
    void register_goods_post_integration_test() throws Exception {
        // given
        Long memberId = member.getId();
        LocationInfo locationInfo = createLocationInfo();
        GoodsPostRequest goodsPostRequest = new GoodsPostRequest(1L, "title", Category.ACCESSORY, 10_000, "content", locationInfo);
        List<MockMultipartFile> files = List.of(createFile(), createFile());

        MockMultipartFile data = new MockMultipartFile(
                "data", "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(goodsPostRequest)
        );

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest = multipart("/api/goods/{memberId}", memberId).file(data);
        files.forEach(multipartRequest::file);

        MockHttpServletResponse result = mockMvc.perform(multipartRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse();
        result.setCharacterEncoding("UTF-8");

        ApiResponse<GoodsPostResponse> apiResponse = objectMapper.readValue(result.getContentAsString(), new TypeReference<>() {});

        // then
        assertApiResponse(apiResponse, goodsPostRequest, files);
        assertActualData(apiResponse.getData().getId(), goodsPostRequest, files);
    }

    @Test
    @DisplayName("굿즈거래 판매글 수정 통합 테스트")
    void update_goods_post_integration_test() throws Exception {
        // given
        Long memberId = member.getId();
        Long goodsPostId = goodsPost.getId();
        LocationInfo locationInfo = createLocationInfo();
        GoodsPostRequest goodsPostRequest = new GoodsPostRequest(1L, "update tile", Category.CAP, 10_000, "update content", locationInfo);
        List<MockMultipartFile> files = List.of(createFile(), createFile(), createFile());

        MockMultipartFile data = new MockMultipartFile(
                "data", "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(goodsPostRequest)
        );

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest
                = multipart("/api/goods/{memberId}/post/{goodsPostId}", memberId, goodsPostId).file(data);
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
                new TypeReference<>() {});

        // then
        assertApiResponse(apiResponse, goodsPostRequest, files);
        assertActualData(apiResponse.getData().getId(), goodsPostRequest, files);
    }

    @Test
    @DisplayName("굿즈거래 판매글 삭제 통합 테스트")
    void delete_goods_post_integration_test() throws Exception {
        // given
        Long memberId = member.getId();
        Long goodsPostId = goodsPost.getId();

        // when
        mockMvc.perform(delete("/api/goods/{memberId}/post/{goodsPostId}", memberId, goodsPostId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // then
        Optional<GoodsPost> goodsPost = goodsPostRepository.findById(goodsPostId);
        assertThat(goodsPost).isEmpty();

        List<String> imageUrls = imageRepository.getImageUrlsByPostId(goodsPostId);
        assertThat(imageUrls).isEmpty();
    }

    // ApiResponse 검증
    private void assertApiResponse(ApiResponse<GoodsPostResponse> apiResponse, GoodsPostRequest expected, List<MockMultipartFile> files) {
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

    private void createMember() {
        member = memberRepository.save(Member.builder()
                .name("홍길동")
                .email("test@gmail.com")
                .nickname("테스터")
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
    }

    private void createGoodsPost() {
        goodsPost = goodsPostRepository.save(GoodsPost.builder()
                .seller(member)
                .teamId(1L)
                .title("test title")
                .content("test content")
                .price(10_000)
                .category(Category.ACCESSORY)
                .location(LocationInfo.toEntity(createLocationInfo()))
                .build());
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile(
                "files",
                "test_photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
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
