package com.example.mate.domain.member.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostImageRepository;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GoodsPostRepository goodsPostRepository;
    @Autowired
    private GoodsPostImageRepository imageRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Member member;

    private GoodsPost goodsPost;

    @BeforeEach
    void setUp() {
        createMember();
        createGoodsPost();
        createGoodsPostImage();
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
                .status(Status.CLOSED)
                .build());
    }

    private void createGoodsPostImage() {
        GoodsPostImage image = GoodsPostImage.builder()
                .imageUrl("upload/test_img_url")
                .build();

        goodsPost.changeImages(List.of(image));
        goodsPostRepository.save(goodsPost);
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

    @Nested
    @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회")
    class ProfileSoldGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 성공")
        void get_sold_goods_page_success() throws Exception {
            // given
            Long memberId = member.getId();
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
                        .status(Status.CLOSED)
                        .build();

                GoodsPostImage image = GoodsPostImage.builder()
                        .imageUrl("upload/test_img_url " + i)
                        .post(post)
                        .build();

                post.changeImages(List.of(image));
                goodsPostRepository.save(post);
            }

            // when
            mockMvc.perform(get("/api/profile/{memberId}/goods/sold", memberId)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_sold_goods_page_invalid_member_id() throws Exception {
            // given
            Long invalidMemberId = member.getId() + 999L; // 존재하지 않는 회원 ID
            int page = 0;
            int size = 10;

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/sold", invalidMemberId)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다")); // 커스텀 에러 메시지
        }
    }

    @Nested
    @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회")
    class ProfileBoughtGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 성공")
        void get_bought_goods_page_success() throws Exception {
            // given
            Member buyer = memberRepository.save(Member.builder()
                    .name("김철수")
                    .email("test2@gmail.com")
                    .nickname("테스터2")
                    .imageUrl("upload/test.jpg")
                    .gender(Gender.MALE)
                    .age(25)
                    .manner(0.3f)
                    .build());
            int page = 0;
            int size = 10;

            goodsPostRepository.deleteAll();
            imageRepository.deleteAll();

            for (int i = 1; i <= 3; i++) {
                GoodsPost post = GoodsPost.builder()
                        .seller(member)
                        .buyer(buyer)
                        .teamId(10L)
                        .title("Test Title " + i)
                        .content("Test Content " + i)
                        .price(1000 * i)
                        .category(Category.ACCESSORY)
                        .location(LocationInfo.toEntity(createLocationInfo()))
                        .status(Status.CLOSED)
                        .build();

                GoodsPostImage image = GoodsPostImage.builder()
                        .imageUrl("upload/test_img_url " + i)
                        .post(post)
                        .build();

                post.changeImages(List.of(image));
                goodsPostRepository.save(post);
            }

            // when
            mockMvc.perform(get("/api/profile/{memberId}/goods/bought", buyer.getId())
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_bought_goods_page_invalid_member_id() throws Exception {
            // given
            Long invalidMemberId = member.getId() + 999L; // 존재하지 않는 회원 ID
            int page = 0;
            int size = 10;

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/bought", invalidMemberId)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다")); // 커스텀 에러 메시지
        }
    }
}