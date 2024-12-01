package com.example.mate.domain.member.service;

import static com.example.mate.common.error.ErrorCode.MEMBER_NOT_FOUND_BY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoodsPostRepository goodsPostRepository;

    private Member seller;
    private Member buyer;
    private GoodsPost goodsPost;
    private GoodsPostImage goodsPostImage;

    @BeforeEach
    void setUp() {
        createTestMember();
        createGoodsPost();
        createGoodsPostImage(goodsPost);
    }

    private void createTestMember() {
        seller = Member.builder()
                .id(1L)
                .name("홍길동")
                .nickname("tester1")
                .email("tester1@example.com")
                .age(20)
                .gender(Gender.MALE)
                .teamId(1L)
                .build();
        buyer = Member.builder()
                .id(2L)
                .name("김영희")
                .nickname("tester2")
                .email("tester2@example.com")
                .age(20)
                .gender(Gender.FEMALE)
                .teamId(2L)
                .build();
    }

    private void createGoodsPost() {
        goodsPost = GoodsPost.builder()
                .id(1L)
                .seller(seller)
                .buyer(buyer)
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

    @Nested
    @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회")
    class ProfileSoldGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 성공")
        void get_sold_goods_page_success() {
            // given
            Long memberId = 1L;

            PageImpl<GoodsPost> soldGoodsPage = new PageImpl<>(List.of(goodsPost));
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(seller));
            given(goodsPostRepository.findGoodsPostsBySellerId(memberId, Status.CLOSED, pageable))
                    .willReturn(soldGoodsPage);

            // when
            PageResponse<MyGoodsRecordResponse> response = profileService.getSoldGoodsPage(memberId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isEqualTo(soldGoodsPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(soldGoodsPage.getContent().size());

            MyGoodsRecordResponse recordResponse = response.getContent().get(0);
            assertThat(recordResponse.getTitle()).isEqualTo(goodsPost.getTitle());
            assertThat(recordResponse.getPrice()).isEqualTo(goodsPost.getPrice());
            assertThat(recordResponse.getImageUrl()).isEqualTo(goodsPostImage.getImageUrl());

            verify(goodsPostRepository).findGoodsPostsBySellerId(memberId, Status.CLOSED, pageable);
        }

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_sold_goods_page_invalid_member_id() {
            // given
            Long invalidMemberId = 999L;
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.getSoldGoodsPage(invalidMemberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MEMBER_NOT_FOUND_BY_ID);

            verify(memberRepository).findById(invalidMemberId);
        }
    }
}