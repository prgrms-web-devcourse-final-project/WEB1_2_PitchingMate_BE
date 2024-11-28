package com.example.mate.domain.goods.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.goods.dto.LocationInfo;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private Member createTestMember() {
        return Member.builder()
                .id(1L)
                .name("홍길동")
                .email("test@gmail.com")
                .nickname("테스터")
                .build();
    }

    private GoodsPostRequest createGoodsPostRequest() {
        LocationInfo location = LocationInfo.builder()
                .placeName("Stadium Plaza")
                .longitude("127.12345")
                .latitude("37.56789")
                .build();

        return new GoodsPostRequest(1L, "title", Category.ACCESSORY, 10_000, "content", location);
    }

    private GoodsPostImage createGoodsPostImage(GoodsPost post) {
        return GoodsPostImage.builder()
                .imageUrl("upload/test_img_url")
                .post(post)
                .build();
    }

    private MockMultipartFile createFile(String contentType) {
        return new MockMultipartFile(
                "file",
                "test_photo.jpg",
                contentType,
                "content".getBytes()
        );
    }

    @Test
    @DisplayName("굿즈거래 판매글 작성 성공")
    void register_goods_post_success() {
        // given
        Member member = createTestMember();
        GoodsPostRequest request = createGoodsPostRequest();
        List<MultipartFile> files = List.of(createFile(MediaType.IMAGE_JPEG_VALUE));

        GoodsPost post = GoodsPostRequest.toEntity(member, request);
        GoodsPostImage goodsPostImage = createGoodsPostImage(post);

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(goodsPostRepository.save(any(GoodsPost.class))).willReturn(post);
        given(imageRepository.save(any(GoodsPostImage.class))).willReturn(goodsPostImage);

        // when
        GoodsPostResponse response = goodsService.registerGoodsPost(member.getId(), request, files);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Status.OPEN.getValue());

        verify(memberRepository).findById(member.getId());
        verify(goodsPostRepository).save(any(GoodsPost.class));
        verify(imageRepository).save(any(GoodsPostImage.class));
    }

    @Test
    @DisplayName("굿즈거래 판매글 작성 실패 - 존재하지 않는 회원")
    void register_goods_post_failed_with_invalid_member() {
        // given
        Long invalidMemberId = 100L;
        GoodsPostRequest request = createGoodsPostRequest();
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
        Member member = createTestMember();
        GoodsPostRequest request = createGoodsPostRequest();
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