package com.example.mate.domain.goods.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostImageRepository;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {

    @InjectMocks
    private GoodsService goodsService;

    @Mock
    private GoodsPostRepository goodsPostRepository;

    @Mock
    private GoodsPostImageRepository imageRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member member;

    private GoodsPost goodsPost;

    private GoodsPostImage goodsPostImage;

    @BeforeEach
    void setUp() {
        createTestMember();
        createGoodsPost();
        createGoodsPostImage(goodsPost);
    }

    private void createTestMember() {
        member = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("test@gmail.com")
                .nickname("테스터")
                .build();
    }

    private void createGoodsPost() {
        goodsPost = GoodsPost.builder()
                .seller(member)
                .teamId(1L)
                .title("test title")
                .content("test content")
                .price(10_000)
                .category(Category.ACCESSORY)
                .location(LocationInfo.toEntity(createLocationInfo()))
                .build();
    }

    private LocationInfo createLocationInfo() {
        return LocationInfo.builder()
                .placeName("Stadium Plaza")
                .longitude("127.12345")
                .latitude("37.56789")
                .build();
    }

    private void createGoodsPostImage(GoodsPost post) {
        goodsPostImage = GoodsPostImage.builder()
                .imageUrl("upload/test_img_url")
                .post(post)
                .build();

        goodsPostImage.setAsMainImage();
        goodsPost.changeImages(List.of(goodsPostImage));
    }

    private MockMultipartFile createFile(String contentType) {
        return new MockMultipartFile(
                "file",
                "test_photo.jpg",
                contentType,
                "content".getBytes()
        );
    }

    @Nested
    @DisplayName("굿즈거래 판매글 작성 테스트")
    class GoodsServiceRegisterTest {
        @Test
        @DisplayName("굿즈거래 판매글 작성 성공")
        void register_goods_post_success() {
            // given
            GoodsPostRequest request = new GoodsPostRequest(1L, "title", Category.ACCESSORY, 10_000, "content", createLocationInfo());
            List<MultipartFile> files = List.of(createFile(MediaType.IMAGE_JPEG_VALUE));

            GoodsPost post = GoodsPostRequest.toEntity(member, request);

            given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
            given(goodsPostRepository.save(any(GoodsPost.class))).willReturn(post);

            // when
            GoodsPostResponse response = goodsService.registerGoodsPost(member.getId(), request, files);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(Status.OPEN.getValue());

            verify(memberRepository).findById(member.getId());
            verify(goodsPostRepository).save(any(GoodsPost.class));
        }

        @Test
        @DisplayName("굿즈거래 판매글 작성 실패 - 존재하지 않는 회원")
        void register_goods_post_failed_with_invalid_member() {
            // given
            Long invalidMemberId = 100L;
            GoodsPostRequest request = new GoodsPostRequest(1L, "title", Category.ACCESSORY, 10_000, "content", createLocationInfo());
            List<MultipartFile> files = List.of(createFile(MediaType.IMAGE_JPEG_VALUE));
            given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> goodsService.registerGoodsPost(invalidMemberId, request, files))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            verify(memberRepository).findById(invalidMemberId);
            verify(goodsPostRepository, never()).save(any());
            verify(imageRepository, never()).save(any());
        }

        @Test
        @DisplayName("굿즈거래 판매글 작성 실패 - 형식이 잘못된 파일")
        void register_goods_post_failed_with_invalid_file() {
            // given
            GoodsPostRequest request = new GoodsPostRequest(1L, "title", Category.ACCESSORY, 10_000, "content", createLocationInfo());
            List<MultipartFile> files = List.of(createFile(MediaType.APPLICATION_PDF_VALUE));
            given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> goodsService.registerGoodsPost(member.getId(), request, files))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FILE_UNSUPPORTED_TYPE.getMessage());

            verify(memberRepository).findById(member.getId());
            verify(goodsPostRepository, never()).save(any());
            verify(imageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("굿즈거래 판매글 수정 테스트")
    class GoodsServiceUpdateTest {

        @Test
        @DisplayName("굿즈거래 판매글 수정 성공")
        void update_goods_post_success() {
            // given
            Long goodsPostId = 1L;
            GoodsPostRequest request = new GoodsPostRequest(1L, "title", Category.CAP, 100_000, "test....", createLocationInfo());
            List<MultipartFile> files = List.of(createFile(MediaType.IMAGE_JPEG_VALUE));

            given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(goodsPost));
            given(imageRepository.getImageUrlsByPostId(goodsPostId)).willReturn(List.of());

            // when
            GoodsPostResponse actual = goodsService.updateGoodsPost(member.getId(), goodsPostId, request, files);

            // then
            assertThat(actual).isNotNull();
            assertThat(actual.getStatus()).isEqualTo(Status.OPEN.getValue());
            assertThat(actual.getContent()).isEqualTo("test....");
            assertThat(actual.getCategory()).isEqualTo(Category.CAP.getValue());
            assertThat(actual.getPrice()).isEqualTo(100_000);

            verify(memberRepository).findById(member.getId());
            verify(imageRepository).getImageUrlsByPostId(goodsPostId);
            verify(imageRepository).deleteAllByPostId(goodsPostId);
        }

        @Test
        @DisplayName("굿즈거래 판매글 수정 실패 - 판매자 회원 불일치")
        void update_goods_post_failed_with_invalid_member() {
            // given
            Long goodsPostId = 1L;
            GoodsPostRequest request = new GoodsPostRequest(1L, "title", Category.CAP, 100_000, "test....", createLocationInfo());
            List<MultipartFile> files = List.of(createFile(MediaType.IMAGE_JPEG_VALUE));
            Member notSeller = Member.builder().id(11L).name("홍길동").build();

            given(memberRepository.findById(member.getId())).willReturn(Optional.of(notSeller));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(goodsPost));

            // when
            assertThatThrownBy(() -> goodsService.updateGoodsPost(member.getId(), goodsPostId, request, files))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_UPDATE_NOT_ALLOWED.getMessage());

            // then
            verify(memberRepository).findById(member.getId());
            verify(goodsPostRepository).findById(goodsPostId);
            verify(imageRepository, never()).deleteAllByPostId(any(Long.class));
            verify(imageRepository, never()).save(any(GoodsPostImage.class));
        }

        @Test
        @DisplayName("굿즈거래 판매글 수정 실패 - 존재하지 않는 판매글")
        void update_goods_post_failed_with_invalid_post() {
            // given
            Long goodsPostId = 1L;
            GoodsPostRequest request = new GoodsPostRequest(1L, "title", Category.CAP, 100_000, "test....", createLocationInfo());
            List<MultipartFile> files = List.of(createFile(MediaType.IMAGE_JPEG_VALUE));

            given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsService.updateGoodsPost(member.getId(), goodsPostId, request, files))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_NOT_FOUND_BY_ID.getMessage());

            // then
            verify(memberRepository).findById(member.getId());
            verify(goodsPostRepository).findById(goodsPostId);
            verify(imageRepository, never()).deleteAllByPostId(any(Long.class));
            verify(imageRepository, never()).save(any(GoodsPostImage.class));
        }
    }

    @Nested
    @DisplayName("굿즈거래 판매글 수정 테스트")
    class GoodsServiceDeleteTest {

        @Test
        @DisplayName("굿즈거래 판매글 삭제 성공")
        void delete_goods_post_success() {
            // given
            Long memberId = member.getId();
            Long goodsPostId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(goodsPost));
            given(imageRepository.getImageUrlsByPostId(goodsPostId)).willReturn(List.of());

            // when
            goodsService.deleteGoodsPost(memberId, goodsPostId);

            // then
            verify(memberRepository).findById(memberId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(imageRepository).getImageUrlsByPostId(goodsPostId);
            verify(imageRepository).deleteAllByPostId(goodsPostId);
            verify(goodsPostRepository).delete(goodsPost);
        }

        @Test
        @DisplayName("굿즈거래 판매글 삭제 실패 - 유효하지 않은 판매글")
        void delete_goods_post_failed_with_invalid_post() {
            // given
            Long memberId = member.getId();
            Long goodsPostId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsService.deleteGoodsPost(memberId, goodsPostId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_NOT_FOUND_BY_ID.getMessage());

            // then
            verify(memberRepository).findById(memberId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(imageRepository, never()).getImageUrlsByPostId(any(Long.class));
            verify(imageRepository, never()).deleteAllByPostId(any(Long.class));
            verify(goodsPostRepository, never()).delete(any(GoodsPost.class));
        }

        @Test
        @DisplayName("굿즈거래 판매글 삭제 실패 - 유효하지 않은 회원")
        void delete_goods_post_failed_with_invalid_member() {
            // given
            Long memberId = member.getId();
            Long goodsPostId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsService.deleteGoodsPost(memberId, goodsPostId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            // then
            verify(memberRepository).findById(memberId);
            verify(goodsPostRepository, never()).findById(any(Long.class));
            verify(imageRepository, never()).getImageUrlsByPostId(any(Long.class));
            verify(imageRepository, never()).deleteAllByPostId(any(Long.class));
            verify(goodsPostRepository, never()).delete(any(GoodsPost.class));
        }
    }

    @Nested
    @DisplayName("굿즈거래 판매글 상세 조회 테스트")
    class GoodsServiceSearchTest {

        @Test
        @DisplayName("굿즈거래 판매글 상세 조회 성공")
        void get_goods_post_success() {
            // given
            Long goodsPostId = 1L;
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(goodsPost));

            // when
            GoodsPostResponse response = goodsService.getGoodsPost(goodsPostId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo(goodsPost.getTitle());
            assertThat(response.getContent()).isEqualTo(goodsPost.getContent());
            assertThat(response.getCategory()).isEqualTo(goodsPost.getCategory().getValue());
            assertThat(response.getPrice()).isEqualTo(goodsPost.getPrice());
            assertThat(response.getLocation().getPlaceName()).isEqualTo(goodsPost.getLocation().getPlaceName());

            verify(goodsPostRepository).findById(goodsPostId);
        }

        @Test
        @DisplayName("굿즈거래 판매글 상세 조회 실패 - 유효하지 않은 판매글")
        void get_goods_post_failed_with_invalid_post() {
            // given
            Long goodsPostId = 1L;

            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> goodsService.getGoodsPost(goodsPostId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_NOT_FOUND_BY_ID.getMessage());

            verify(goodsPostRepository).findById(goodsPostId);
        }
    }

    @Nested
    @DisplayName("메인페이지 굿즈거래 판매글 조회 테스트")
    class GoodsServiceMainPageTest {

        @Test
        @DisplayName("메인페이지 굿즈거래 판매글 조회 성공")
        void get_main_goods_posts_success() {
            // given
            Long teamId = 1L;
            List<GoodsPost> goodsPosts = List.of(goodsPost);
            given(goodsPostRepository.findMainGoodsPosts(1L, Status.OPEN, PageRequest.of(0, 4))).willReturn(goodsPosts);

            // when
            List<GoodsPostSummaryResponse> responses = goodsService.getMainGoodsPosts(teamId);

            // then
            assertThat(responses).isNotEmpty();
            assertThat(responses.size()).isEqualTo(goodsPosts.size());

            GoodsPostSummaryResponse goodsPostSummaryResponse = responses.get(0);
            assertThat(goodsPostSummaryResponse.getTitle()).isEqualTo(goodsPost.getTitle());
            assertThat(goodsPostSummaryResponse.getPrice()).isEqualTo(goodsPost.getPrice());
            assertThat(goodsPostSummaryResponse.getImageUrl()).isEqualTo(goodsPostImage.getImageUrl());

            verify(goodsPostRepository).findMainGoodsPosts(1L, Status.OPEN, PageRequest.of(0, 4));
        }

        @Test
        @DisplayName("메인페이지 굿즈거래 판매글 조회 성공 - 대표 이미지가 없는 경우 기본 이미지를 반환")
        void get_main_goods_posts_success_with_no_main_image() {
            // given
            Long teamId = 1L;
            List<GoodsPostImage> imagesWithoutMain = List.of(
                    GoodsPostImage.builder().imageUrl("upload/test_img_url_1").post(goodsPost).build(),
                    GoodsPostImage.builder().imageUrl("upload/test_img_url_2").post(goodsPost).build()
            );
            // 직접 엔티티에 추가하여, 대표 사진 마설정
            GoodsPost post = GoodsPost.builder()
                    .id(1L)
                    .teamId(1L)
                    .title("test title")
                    .category(Category.ACCESSORY)
                    .price(10_000)
                    .goodsPostImages(imagesWithoutMain).build();

            List<GoodsPost> goodsPosts = List.of(post);
            given(goodsPostRepository.findMainGoodsPosts(teamId, Status.OPEN, PageRequest.of(0, 4))).willReturn(
                    goodsPosts);

            // when
            List<GoodsPostSummaryResponse> responses = goodsService.getMainGoodsPosts(teamId);

            // then
            assertThat(responses).isNotEmpty();
            assertThat(responses.size()).isEqualTo(goodsPosts.size());

            GoodsPostSummaryResponse goodsPostSummaryResponse = responses.get(0);
            assertThat(goodsPostSummaryResponse.getTitle()).isEqualTo(goodsPost.getTitle());
            assertThat(goodsPostSummaryResponse.getPrice()).isEqualTo(goodsPost.getPrice());
            assertThat(goodsPostSummaryResponse.getImageUrl()).isEqualTo("upload/default.jpg");

            verify(goodsPostRepository).findMainGoodsPosts(1L, Status.OPEN, PageRequest.of(0, 4));
        }
    }

    @Nested
    @DisplayName("메인페이지 굿즈거래 판매글 페이징 조회 테스트")
    class GoodsServiceGoodsPageTest {

        private GoodsPost createGoodsPostWithoutFilters() {
            return GoodsPost.builder()
                    .seller(member)
                    .teamId(10L)
                    .title("No Filter Title")
                    .content("No Filter Content")
                    .price(10_000)
                    .category(Category.UNIFORM)
                    .location(LocationInfo.toEntity(createLocationInfo()))
                    .build();
        }

        private GoodsPost createGoodsPostWithFilters() {
            return GoodsPost.builder()
                    .seller(member)
                    .teamId(1L)
                    .title("Filtered Title")
                    .content("Filtered Content")
                    .price(20_000)
                    .category(Category.ACCESSORY)
                    .location(LocationInfo.toEntity(createLocationInfo()))
                    .build();
        }

        @Test
        @DisplayName("메인페이지 굿즈거래 판매글 페이징 조회 성공 - 필터 비활성화")
        void get_page_goods_posts_no_filters() {
            // given
            Long teamId = null;         // 팀 필터 비활성화
            Category category = null;   // 카테고리 필터 비활성화
            GoodsPost postWithoutFilters = createGoodsPostWithoutFilters();
            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl("upload/test_img_url")
                    .post(postWithoutFilters)
                    .build();
            postWithoutFilters.changeImages(List.of(image));

            PageImpl<GoodsPost> goodsPostPage = new PageImpl<>(List.of(postWithoutFilters));
            PageRequest pageRequest = PageRequest.of(0, 10);

            given(goodsPostRepository.findPageGoodsPosts(teamId, Status.OPEN, category, pageRequest)).willReturn(goodsPostPage);

            // when
            PageResponse<GoodsPostSummaryResponse> pageGoodsPosts = goodsService.getPageGoodsPosts(teamId, null, pageRequest);

            // then
            assertThat(pageGoodsPosts).isNotNull();
            assertThat(pageGoodsPosts.getContent()).isNotEmpty();
            assertThat(pageGoodsPosts.getTotalElements()).isEqualTo(goodsPostPage.getTotalElements());
            assertThat(pageGoodsPosts.getContent().size()).isEqualTo(goodsPostPage.getContent().size());

            GoodsPostSummaryResponse summary = pageGoodsPosts.getContent().get(0);
            assertThat(summary.getTitle()).isEqualTo(postWithoutFilters.getTitle());
            assertThat(summary.getPrice()).isEqualTo(postWithoutFilters.getPrice());
            assertThat(summary.getImageUrl()).isEqualTo(image.getImageUrl());

            verify(goodsPostRepository).findPageGoodsPosts(teamId, Status.OPEN, category, pageRequest);
        }

        @Test
        @DisplayName("메인페이지 굿즈거래 판매글 페이징 조회 성공 - 필터 활성화")
        void get_page_goods_posts_all_filters() {
            // given
            Long teamId = 1L;
            Category category = Category.ACCESSORY;
            GoodsPost postWithFilters = createGoodsPostWithFilters();
            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl("upload/test_img_url")
                    .post(postWithFilters)
                    .build();
            postWithFilters.changeImages(List.of(image));

            PageImpl<GoodsPost> goodsPostPage = new PageImpl<>(List.of(postWithFilters));
            PageRequest pageRequest = PageRequest.of(0, 10);

            given(goodsPostRepository.findPageGoodsPosts(teamId, Status.OPEN, category, pageRequest)).willReturn(goodsPostPage);

            // when
            PageResponse<GoodsPostSummaryResponse> pageGoodsPosts
                    = goodsService.getPageGoodsPosts(teamId, category.getValue(), pageRequest);

            // then
            assertThat(pageGoodsPosts).isNotNull();
            assertThat(pageGoodsPosts.getContent()).isNotEmpty();
            assertThat(pageGoodsPosts.getTotalElements()).isEqualTo(goodsPostPage.getTotalElements());
            assertThat(pageGoodsPosts.getContent().size()).isEqualTo(goodsPostPage.getContent().size());

            GoodsPostSummaryResponse summary = pageGoodsPosts.getContent().get(0);
            assertThat(summary.getTitle()).isEqualTo(postWithFilters.getTitle());
            assertThat(summary.getPrice()).isEqualTo(postWithFilters.getPrice());
            assertThat(summary.getImageUrl()).isEqualTo(image.getImageUrl());

            verify(goodsPostRepository).findPageGoodsPosts(teamId, Status.OPEN, category, pageRequest);
        }

        @Test
        @DisplayName("메인페이지 굿즈거래 판매글 페이징 조회 성공 - 유효하지 않은 팀 정보")
        void get_page_goods_posts_team_filter_only() {
            // given
            Long teamId = 999L;
            Category category = Category.ACCESSORY;
            PageImpl<GoodsPost> goodsPostPage = new PageImpl<>(List.of(goodsPost));
            PageRequest pageRequest = PageRequest.of(0, 10);

            // when & then
            assertThatThrownBy(() -> goodsService.getPageGoodsPosts(teamId, category.getValue(), pageRequest))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.TEAM_NOT_FOUND.getMessage());
        }
    }
}