package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goodsPost.dto.response.LocationInfo;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.goodsReview.repository.GoodsReviewRepositoryCustom;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.matePost.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.matePost.entity.*;
import com.example.mate.domain.matePost.repository.*;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.dto.response.MyTimelineResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse.MateReviewResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.mate.common.error.ErrorCode.MEMBER_NOT_FOUND_BY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoodsPostRepository goodsPostRepository;

    @Mock
    private MateRepository mateRepository;

    @Mock
    private MateReviewRepositoryCustom mateReviewRepositoryCustom;

    @Mock
    private GoodsReviewRepositoryCustom goodsReviewRepositoryCustom;

    @Mock
    private VisitPartRepository visitPartRepository;

    @Mock
    private MateReviewRepository mateReviewRepository;

    @Mock
    private VisitRepository visitRepository;

    private Member member1;
    private Member member2;
    private Member member3;
    private GoodsPost goodsPost;
    private MatePost matePost;
    private GoodsPostImage goodsPostImage;
    private MateReview mateReview;

    @BeforeEach
    void setUp() {
        createTestMember();
        createGoodsPost();
        createGoodsPostImage(goodsPost);
        createMatePost();
        createMateReview();
    }

    private void createTestMember() {
        member1 = Member.builder()
                .id(1L)
                .name("홍길동")
                .nickname("tester1")
                .email("tester1@example.com")
                .age(20)
                .gender(Gender.MALE)
                .teamId(1L)
                .build();
        member2 = Member.builder()
                .id(2L)
                .name("김영희")
                .nickname("tester2")
                .email("tester2@example.com")
                .age(20)
                .gender(Gender.FEMALE)
                .teamId(2L)
                .build();
        member3 = Member.builder()
                .id(3L)
                .name("안영희")
                .nickname("tester3")
                .email("tester3@example.com")
                .age(20)
                .gender(Gender.FEMALE)
                .teamId(2L)
                .build();
    }

    private void createGoodsPost() {
        goodsPost = GoodsPost.builder()
                .id(1L)
                .seller(member1)
                .buyer(member2)
                .teamId(1L)
                .title("test title")
                .content("test content")
                .price(10_000)
                .category(Category.ACCESSORY)
                .location(LocationInfo.toEntity(createLocationInfo()))
                .build();
    }

    private void createMatePost() {
        matePost = MatePost.builder()
                .id(1L)
                .author(member1)
                .teamId(1L)
                .match(Match.builder()
                        .homeTeamId(1L)
                        .awayTeamId(2L)
                        .stadiumId(1L)
                        .matchTime(LocalDateTime.now().plusDays(1))
                        .build())
                .title("test title")
                .content("test content")
                .imageUrl(goodsPostImage.getImageUrl())
                .status(com.example.mate.domain.matePost.entity.Status.OPEN)
                .maxParticipants(4)
                .age(Age.TWENTIES)
                .gender(Gender.FEMALE)
                .transport(TransportType.PUBLIC)
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

        goodsPost.changeImages(List.of(goodsPostImage));
    }

    private void createMateReview() {
        mateReview = MateReview.builder()
                .id(1L)
                .reviewer(member2)
                .reviewee(member1)
                .reviewContent("good")
                .rating(Rating.GOOD)
                .build();
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

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member1));
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
            assertThat(recordResponse.getImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl(goodsPostImage.getImageUrl()));

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

    @Nested
    @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회")
    class ProfileBoughtGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 성공")
        void get_bought_goods_page_success() {
            // given
            Long memberId = 2L;

            PageImpl<GoodsPost> boughtGoodsPage = new PageImpl<>(List.of(goodsPost));
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member1));
            given(goodsPostRepository.findGoodsPostsByBuyerId(memberId, Status.CLOSED, pageable))
                    .willReturn(boughtGoodsPage);

            // when
            PageResponse<MyGoodsRecordResponse> response = profileService.getBoughtGoodsPage(memberId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isEqualTo(boughtGoodsPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(boughtGoodsPage.getContent().size());

            MyGoodsRecordResponse recordResponse = response.getContent().get(0);
            assertThat(recordResponse.getTitle()).isEqualTo(goodsPost.getTitle());
            assertThat(recordResponse.getPrice()).isEqualTo(goodsPost.getPrice());
            assertThat(recordResponse.getImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl(goodsPostImage.getImageUrl()));

            verify(goodsPostRepository).findGoodsPostsByBuyerId(memberId, Status.CLOSED, pageable);
        }

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_bought_goods_page_invalid_member_id() {
            // given
            Long invalidMemberId = 999L;
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.getBoughtGoodsPage(invalidMemberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MEMBER_NOT_FOUND_BY_ID);

            verify(memberRepository).findById(invalidMemberId);
        }
    }

    @Nested
    @DisplayName("회원 프로필 메이트 후기 페이징 조회")
    class ProfileMateReviewPage {

        @Test
        @DisplayName("회원 프로필 메이트 후기 페이징 조회 성공")
        void get_mate_review_page_success() {
            // given
            Long memberId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            List<MyReviewResponse> myReviewResponseList = List.of(
                    new MyReviewResponse(1L, "Title 1", "tester1", "GOOD", "Great!", LocalDateTime.now()),
                    new MyReviewResponse(2L, "Title 2", "tester2", "GOOD", "Nice!", LocalDateTime.now())
            );

            Page<MyReviewResponse> mateReviewPage = new PageImpl<>(myReviewResponseList, pageable,
                    myReviewResponseList.size());

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member1));
            given(mateReviewRepositoryCustom.findMateReviewsByRevieweeId(memberId, pageable))
                    .willReturn(mateReviewPage);

            // when
            PageResponse<MyReviewResponse> response = profileService.getMateReviewPage(memberId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isEqualTo(mateReviewPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(mateReviewPage.getContent().size());

            MyReviewResponse reviewResponse = response.getContent().get(0);
            assertThat(reviewResponse.getTitle()).isEqualTo(myReviewResponseList.get(0).getTitle());
            assertThat(reviewResponse.getContent()).isEqualTo(myReviewResponseList.get(0).getContent());

            verify(mateReviewRepositoryCustom).findMateReviewsByRevieweeId(memberId, pageable);
        }

        @Test
        @DisplayName("회원 프로필 메이트 후기 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_mate_review_page_fail_invalid_member_id() {
            // given
            Long invalidMemberId = 999L; // 존재하지 않는 회원 ID
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.getMateReviewPage(invalidMemberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MEMBER_NOT_FOUND_BY_ID);

            verify(memberRepository).findById(invalidMemberId);

        }
    }

    @Nested
    @DisplayName("회원 프로필 굿즈 후기 페이징 조회")
    class ProfileGoodsReviewPage {

        @Test
        @DisplayName("회원 프로필 굿즈 후기 페이징 조회 성공")
        void get_goods_review_page_success() {
            // given
            Long memberId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            List<MyReviewResponse> myReviewResponseList = List.of(
                    new MyReviewResponse(1L, "Title 1", "tester1", "GOOD", "Great!", LocalDateTime.now()),
                    new MyReviewResponse(2L, "Title 2", "tester2", "GOOD", "Nice!", LocalDateTime.now())
            );

            Page<MyReviewResponse> goodsReviewPage = new PageImpl<>(myReviewResponseList, pageable,
                    myReviewResponseList.size());

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member1));
            given(goodsReviewRepositoryCustom.findGoodsReviewsByRevieweeId(memberId, pageable))
                    .willReturn(goodsReviewPage);

            // when
            PageResponse<MyReviewResponse> response = profileService.getGoodsReviewPage(memberId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isEqualTo(goodsReviewPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(goodsReviewPage.getContent().size());

            MyReviewResponse reviewResponse = response.getContent().get(0);
            assertThat(reviewResponse.getTitle()).isEqualTo(myReviewResponseList.get(0).getTitle());
            assertThat(reviewResponse.getContent()).isEqualTo(myReviewResponseList.get(0).getContent());

            verify(goodsReviewRepositoryCustom).findGoodsReviewsByRevieweeId(memberId, pageable);
        }

        @Test
        @DisplayName("회원 프로필 굿즈 후기 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_goods_review_page_fail_invalid_member_id() {
            // given
            Long invalidMemberId = 999L; // 존재하지 않는 회원 ID
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.getGoodsReviewPage(invalidMemberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MEMBER_NOT_FOUND_BY_ID);

            verify(memberRepository).findById(invalidMemberId);

        }
    }

    @Nested
    @DisplayName("회원 타임라인 페이징 조회")
    class ProfileTimelinePage {

        @Test
        @DisplayName("회원 타임라인 페이징 조회 성공")
        void get_my_visit_page_success() {
            // given
            Long memberId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            // memberRepository의 mock 설정
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member1));

            // timelineRepositoryCustom의 mock 설정
            List<MyTimelineResponse> myTimelineResponseList = List.of(
                    new MyTimelineResponse(1L, 1L, 1L)
            );
            Page<MyTimelineResponse> visitsByIdPage = new PageImpl<>(
                    myTimelineResponseList, pageable, myTimelineResponseList.size());
            given(visitRepository.findVisitsByMemberId(memberId, pageable)).willReturn(visitsByIdPage);

            // match mock 설정
            List<Match> matchesByMatePostId = List.of(Match.builder()
                    .id(1L)
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(1L)
                    .build());
            given(visitRepository.findMatchesByMemberId(memberId)).willReturn(matchesByMatePostId);

            // mateRepository의 mock 설정
            Match match = Match.builder().homeTeamId(1L).awayTeamId(2L).stadiumId(1L).build();

            Visit visit = Visit.builder().id(1L).build();

            List<MateReview> existReviews = List.of(
                    MateReview.builder()
                            .id(1L)
                            .visit(visit)
                            .reviewer(member1)
                            .reviewee(member2)
                            .rating(Rating.GOOD)
                            .build(),
                    MateReview.builder()
                            .id(2L)
                            .visit(visit)
                            .reviewer(member1)
                            .reviewee(member3)
                            .rating(Rating.GOOD)
                            .build());
            given(mateReviewRepository.findMateReviewsByVisitIdAndReviewerId(1L, 1L))
                    .willReturn(existReviews);

            // visitPartRepository의 mock 설정
            given(visitPartRepository.findMembersByVisitIdExcludeMember(1L, memberId)).willReturn(
                    List.of(member2, member3));

            // when
            PageResponse<MyVisitResponse> response = profileService.getMyVisitPage(memberId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isEqualTo(visitsByIdPage.getTotalElements());
            assertThat(response.getTotalPages()).isEqualTo(visitsByIdPage.getTotalPages());
            assertThat(response.getContent().size()).isEqualTo(myTimelineResponseList.size());

            MyVisitResponse visitResponse = response.getContent().get(0);
            assertThat(visitResponse.getHomeTeamName()).isEqualTo(TeamInfo.getById(match.getHomeTeamId()).shortName);
            assertThat(visitResponse.getAwayTeamName()).isEqualTo(TeamInfo.getById(match.getAwayTeamId()).shortName);
            assertThat(visitResponse.getLocation()).isEqualTo(match.getStadium().name);
            assertThat(visitResponse.getMatchTime()).isEqualTo(match.getMatchTime());

            // 리뷰 정보 검증
            assertThat(visitResponse.getReviews()).hasSize(2);
            MateReviewResponse review1 = visitResponse.getReviews().get(0);
            assertThat(review1.getMemberId()).isEqualTo(member2.getId());
            assertThat(review1.getNickname()).isEqualTo(member2.getNickname());
        }

        @Test
        @DisplayName("회원 타임라인 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_my_visit_page_fail_invalid_member_id() {
            // given
            Long invalidMemberId = 999L; // 존재하지 않는 회원 ID
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.getMyVisitPage(invalidMemberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            MEMBER_NOT_FOUND_BY_ID); // MEMBER_NOT_FOUND_BY_ID는 예외 처리 시 사용된 errorCode로, 실제 코드에 맞게 설정해주세요.

            verify(memberRepository).findById(invalidMemberId);
        }
    }

    @Nested
    @DisplayName("작성한 굿즈 거래글 페이징 조회")
    class ProfileGoodsPostsPage {

        @Test
        @DisplayName("작성한 굿즈 거래글 페이징 조회 성공")
        void get_my_goods_posts_page_success() {
            // given
            Long memberId = 1L;
            PageImpl<GoodsPost> goodsPostsPage = new PageImpl<>(List.of(goodsPost));
            Pageable pageable = PageRequest.of(0, 10);

            given(goodsPostRepository.findMyGoodsPosts(memberId, pageable))
                    .willReturn(goodsPostsPage);

            // when
            PageResponse<GoodsPostSummaryResponse> response = profileService.getGoodsPostsPage(memberId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isEqualTo(goodsPostsPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(goodsPostsPage.getContent().size());

            GoodsPostSummaryResponse postResponse = response.getContent().get(0);
            assertThat(postResponse.getTitle()).isEqualTo(goodsPost.getTitle());
            assertThat(postResponse.getPrice()).isEqualTo(goodsPost.getPrice());
            assertThat(postResponse.getImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl(goodsPostImage.getImageUrl()));

            verify(goodsPostRepository).findMyGoodsPosts(memberId, pageable);
        }
    }

    @Nested
    @DisplayName("작성한 메이트 구인글 페이징 조회")
    class ProfileMatePostsPage {

        @Test
        @DisplayName("작성한 메이트 구인글 페이징 조회 성공")
        void get_my_mate_posts_page_success() {
            // given
            Long memberId = 1L;
            PageImpl<MatePost> matePostsPage = new PageImpl<>(List.of(matePost));
            Pageable pageable = PageRequest.of(0, 10);

            given(mateRepository.findMyMatePosts(memberId, pageable)).willReturn(matePostsPage);

            // when
            PageResponse<MatePostSummaryResponse> response = profileService.getMatePostsPage(memberId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isEqualTo(matePostsPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(matePostsPage.getContent().size());

            MatePostSummaryResponse postResponse = response.getContent().get(0);
            assertThat(postResponse.getTitle()).isEqualTo(matePost.getTitle());
            assertThat(postResponse.getImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl(goodsPostImage.getImageUrl()));

            verify(mateRepository).findMyMatePosts(memberId, pageable);
        }
    }
}