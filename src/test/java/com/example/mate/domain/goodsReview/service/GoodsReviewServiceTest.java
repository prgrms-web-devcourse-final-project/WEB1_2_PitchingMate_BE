package com.example.mate.domain.goodsReview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.goodsReview.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goodsReview.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goodsReview.entity.GoodsReview;
import com.example.mate.domain.goodsReview.repository.GoodsReviewRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoodsReviewServiceTest {

    @InjectMocks
    private GoodsReviewService goodsReviewService;

    @Mock
    private GoodsPostRepository goodsPostRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoodsReviewRepository reviewRepository;

    private Member member;
    private Member seller;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("test@gmail.com")
                .nickname("테스터")
                .build();
        seller = Member.builder()
                .id(2L)
                .name("김철수")
                .email("seller@gmail.com")
                .nickname("셀러")
                .manner(0.300F)
                .build();
    }

    @Nested
    @DisplayName("굿즈거래 후기 등록 테스트")
    class GoodsPostServiceReviewTest {

        private GoodsPost createGoodsPost(Long id, Status status) {
            return GoodsPost.builder()
                    .id(id)
                    .status(status)
                    .buyer(member)
                    .seller(seller)
                    .title("Test Post")
                    .content("Test Content")
                    .price(10_000)
                    .category(Category.ACCESSORY)
                    .build();
        }

        private GoodsReview createGoodsReview(GoodsPost goodsPost) {
            return GoodsReview.builder()
                    .id(1L)
                    .reviewer(member)
                    .goodsPost(goodsPost)
                    .rating(Rating.GOOD)
                    .reviewContent("Great seller!")
                    .build();
        }

        @DisplayName("굿즈거래 후기 등록 성공")
        @Test
        void registerGoodsReviewSuccess() {
            // given
            GoodsPost completePost = createGoodsPost(2L, Status.CLOSED);
            GoodsReviewRequest request = new GoodsReviewRequest(Rating.GOOD, "Great seller!");
            Long reviewerId = member.getId();
            Long goodsPostId = completePost.getId();
            GoodsReview goodsReview = createGoodsReview(completePost);

            given(memberRepository.findById(reviewerId)).willReturn(Optional.of(member));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(completePost));
            given(reviewRepository.existsByGoodsPostIdAndReviewerId(goodsPostId, reviewerId)).willReturn(false);
            given(reviewRepository.save(any(GoodsReview.class))).willReturn(goodsReview);
            given(memberRepository.save(any(Member.class))).willReturn(seller);

            // when
            GoodsReviewResponse response = goodsReviewService.registerGoodsReview(reviewerId, goodsPostId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReviewerNickname()).isEqualTo(member.getNickname());
            assertThat(response.getRating()).isEqualTo(request.getRating());
            assertThat(response.getReviewContent()).isEqualTo(request.getReviewContent());
            assertThat(seller.getManner()).isCloseTo(0.32f, within(0.0001f));

            verify(memberRepository).findById(reviewerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(reviewRepository).existsByGoodsPostIdAndReviewerId(goodsPostId, reviewerId);
            verify(reviewRepository).save(any(GoodsReview.class));
            verify(memberRepository).save(any(Member.class));
        }

        @DisplayName("굿즈거래 후기 등록 실패 - 이미 작성된 후기")
        @Test
        void registerGoodsReviewFailedDueToDuplicateReview() {
            // given
            GoodsPost completePost = createGoodsPost(2L, Status.CLOSED);
            GoodsReviewRequest request = new GoodsReviewRequest(Rating.GOOD, "Duplicate review!");
            Long reviewerId = member.getId();
            Long goodsPostId = completePost.getId();

            given(memberRepository.findById(reviewerId)).willReturn(Optional.of(member));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(completePost));
            given(reviewRepository.existsByGoodsPostIdAndReviewerId(goodsPostId, reviewerId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> goodsReviewService.registerGoodsReview(reviewerId, goodsPostId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_REVIEW_ALREADY_EXISTS.getMessage());

            verify(memberRepository).findById(reviewerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(reviewRepository).existsByGoodsPostIdAndReviewerId(goodsPostId, reviewerId);
            verify(reviewRepository, never()).save(any());
        }

        @DisplayName("굿즈거래 후기 등록 실패 - 작성자가 구매자가 아닌 경우")
        @Test
        void registerGoodsReviewFailedDueToInvalidReviewer() {
            // given
            GoodsPost completePost = createGoodsPost(2L, Status.CLOSED);
            GoodsReviewRequest request = new GoodsReviewRequest(Rating.GOOD, "Not the buyer review!");
            Member nonBuyer = Member.builder().id(3L).nickname("Non Buyer").build();
            Long reviewerId = nonBuyer.getId();
            Long goodsPostId = completePost.getId();

            given(memberRepository.findById(reviewerId)).willReturn(Optional.of(nonBuyer));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(completePost));

            // when & then
            assertThatThrownBy(() -> goodsReviewService.registerGoodsReview(reviewerId, goodsPostId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_REVIEW_NOT_ALLOWED_FOR_NON_BUYER.getMessage());

            verify(memberRepository).findById(reviewerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(reviewRepository, never()).existsByGoodsPostIdAndReviewerId(any(), any());
            verify(reviewRepository, never()).save(any());
        }

        @DisplayName("굿즈거래 후기 등록 실패 - 판매글의 상태가 거래완료가 아닌 경우")
        @Test
        void registerGoodsReviewFailedDueToInvalidGoodsPostStatus() {
            // given
            GoodsPost incompletePost = createGoodsPost(3L, Status.OPEN); // 상태가 OPEN
            GoodsReviewRequest request = new GoodsReviewRequest(Rating.GOOD, "Not completed transaction");
            Long reviewerId = member.getId();
            Long goodsPostId = incompletePost.getId();

            given(memberRepository.findById(reviewerId)).willReturn(Optional.of(member));
            given(goodsPostRepository.findById(goodsPostId)).willReturn(Optional.of(incompletePost));

            // when & then
            assertThatThrownBy(() -> goodsReviewService.registerGoodsReview(reviewerId, goodsPostId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_REVIEW_STATUS_NOT_CLOSED.getMessage());

            verify(memberRepository).findById(reviewerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(reviewRepository, never()).existsByGoodsPostIdAndReviewerId(any(), any());
            verify(reviewRepository, never()).save(any());
        }
    }
}