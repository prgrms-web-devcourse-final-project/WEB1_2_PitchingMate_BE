package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileService;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsReview;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goods.repository.GoodsReviewRepository;
import com.example.mate.domain.mate.entity.MateReview;
import com.example.mate.domain.mate.entity.VisitPart;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mate.repository.VisitPartRepository;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.dto.request.MemberInfoUpdateRequest;
import com.example.mate.domain.member.dto.request.MemberLoginRequest;
import com.example.mate.domain.member.dto.response.JoinResponse;
import com.example.mate.domain.member.dto.response.MemberLoginResponse;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
import com.example.mate.domain.member.dto.response.MyProfileResponse;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoodsPostRepository goodsPostRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private GoodsReviewRepository goodsReviewRepository;

    @Mock
    private MateReviewRepository mateReviewRepository;

    @Mock
    private VisitPartRepository visitPartRepository;

    @Mock
    private FileService fileService;

    @Mock
    private JwtUtil jwtUtil;

    private Member member;
    private Member member2;
    private Member member3;
    private GoodsPost goodsPost;
    private JoinRequest joinRequest;
    private Follow follow;
    private Follow follow2;
    private Follow follow3;
    private GoodsReview goodsReview;
    private MateReview mateReview;
    private VisitPart visitPart;

    @BeforeEach
    void setUp() {
        createTestMember();
        createTestGoodsPost();
        createTestJoinRequest();
        createTestFollow();
        createTestGoodsReview();
        createTestMateReview();
        createTestVisitPart();
    }

    private void createTestMember() {
        member = Member.builder()
                .id(1L)
                .imageUrl("image.png")
                .name("홍길동")
                .nickname("tester")
                .imageUrl("image.png")
                .email("test@example.com")
                .age(30)
                .gender(Gender.MALE)
                .teamId(1L)
                .manner(0.300F)
                .build();
        member2 = Member.builder()
                .id(2L)
                .name("김철수")
                .nickname("tester2")
                .email("test2@example.com")
                .age(20)
                .gender(Gender.MALE)
                .teamId(2L)
                .manner(0.300F)
                .build();
        member3 = Member.builder()
                .id(3L)
                .name("김영수")
                .nickname("tester3")
                .email("test3@example.com")
                .age(30)
                .gender(Gender.MALE)
                .teamId(3L)
                .manner(0.300F)
                .build();
    }

    private void createTestGoodsPost() {
        goodsPost = GoodsPost.builder()
                .id(1L)
                .seller(member)
                .status(Status.CLOSED)
                .build();
    }

    private void createTestJoinRequest() {
        joinRequest = JoinRequest.builder()
                .name("홍길동")
                .email("test@example.com")
                .gender("M")
                .birthyear("1993")
                .teamId(1L)
                .nickname("tester")
                .build();
    }

    private void createTestFollow() {
        follow = Follow.builder()
                .id(1L)
                .follower(member)
                .following(member2)
                .build();
        follow2 = Follow.builder()
                .id(2L)
                .follower(member2)
                .following(member)
                .build();
        follow3 = Follow.builder()
                .id(3L)
                .follower(member3)
                .following(member)
                .build();
    }

    private void createTestGoodsReview() {
        goodsReview = GoodsReview.builder()
                .id(1L)
                .goodsPost(goodsPost)
                .reviewer(member2)
                .reviewee(member)
                .rating(Rating.GOOD)
                .reviewContent("good")
                .build();
    }

    private void createTestMateReview() {
        mateReview = MateReview.builder()
                .id(1L)
                .reviewer(member3)
                .reviewee(member)
                .rating(Rating.GOOD)
                .reviewContent("good")
                .build();
    }

    private void createTestVisitPart() {
        visitPart = VisitPart.builder()
                .member(member)
                .build();
    }

    private MockMultipartFile createFile(String contentType) {
        return new MockMultipartFile(
                "image",
                "test_photo.jpg",
                contentType,
                "content".getBytes()
        );
    }

    @Test
    @DisplayName("자체 회원 가입 성공")
    void join_success() {
        // given
        given(memberRepository.save(any(Member.class)))
                .willReturn(member);

        // when
        JoinResponse response = memberService.join(joinRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
    }

    @Nested
    @DisplayName("내 프로필 조회")
    class GetMyProfile {

        @Test
        @DisplayName("내 프로필 조회 성공")
        void get_my_profile_success() {
            // given
            Long memberId = 1L;
            int followCount = 1;
            int followerCount = 2;
            int goodsReviewsCount = 1;
            int mateReviewsCount = 1;
            int reviewsCount = goodsReviewsCount + mateReviewsCount;
            int goodsSoldCount = 1;
            int goodsBoughtCount = 0;
            int visitsCount = 1;

            given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
            given(followRepository.countByFollowerId(memberId)).willReturn(followCount);
            given(followRepository.countByFollowingId(memberId)).willReturn(followerCount);
            given(goodsReviewRepository.countByRevieweeId(memberId)).willReturn(goodsReviewsCount);
            given(mateReviewRepository.countByRevieweeId(memberId)).willReturn(mateReviewsCount);
            given(goodsPostRepository.countGoodsPostsBySellerIdAndStatus(memberId, Status.CLOSED)).
                    willReturn(goodsSoldCount);
            given(goodsPostRepository.countGoodsPostsByBuyerIdAndStatus(memberId, Status.CLOSED)).
                    willReturn(goodsBoughtCount);
            given(visitPartRepository.countByMember(member)).
                    willReturn(visitsCount);

            // when
            MyProfileResponse response = memberService.getMyProfile(memberId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getNickname()).isEqualTo(member.getNickname());
            assertThat(response.getManner()).isEqualTo(member.getManner());
            assertThat(response.getAboutMe()).isEqualTo(member.getAboutMe());
            assertThat(response.getFollowingCount()).isEqualTo(followCount);
            assertThat(response.getFollowerCount()).isEqualTo(followerCount);
            assertThat(response.getReviewsCount()).isEqualTo(reviewsCount);
            assertThat(response.getGoodsSoldCount()).isEqualTo(goodsSoldCount);
            assertThat(response.getGoodsBoughtCount()).isEqualTo(goodsBoughtCount);
            assertThat(response.getVisitsCount()).isEqualTo(visitsCount);
        }

        @Test
        @DisplayName("내 프로필 조회 실패 - 회원이 존재하지 않는 경우")
        void get_my_profile_fail_member_not_found() {
            // given
            Long memberId = 999L;  // 존재하지 않는 회원 ID

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMyProfile(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());
        }
    }

    @Nested
    @DisplayName("다른 회원 프로필 조회")
    class GetMemberProfile {

        @Test
        @DisplayName("다른 회원 프로필 조회 성공")
        void get_member_profile_success() {
            // given
            Long memberId = 1L;
            int followCount = 10;
            int followerCount = 20;
            int goodsReviewsCount = 7;
            int mateReviewsCount = 8;
            int reviewsCount = goodsReviewsCount + mateReviewsCount;
            int goodsSoldCount = 1;

            given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
            given(followRepository.countByFollowerId(memberId)).willReturn(followCount);
            given(followRepository.countByFollowingId(memberId)).willReturn(followerCount);
            given(goodsReviewRepository.countByRevieweeId(memberId)).willReturn(goodsReviewsCount);
            given(mateReviewRepository.countByRevieweeId(memberId)).willReturn(mateReviewsCount);
            given(goodsPostRepository.countGoodsPostsBySellerIdAndStatus(memberId, Status.CLOSED)).
                    willReturn(goodsSoldCount);

            // when
            MemberProfileResponse result = memberService.getMemberProfile(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getNickname()).isEqualTo(member.getNickname());
            assertThat(result.getFollowingCount()).isEqualTo(followCount);
            assertThat(result.getFollowerCount()).isEqualTo(followerCount);
            assertThat(result.getReviewsCount()).isEqualTo(reviewsCount);
            assertThat(result.getGoodsSoldCount()).isEqualTo(goodsSoldCount);
        }

        @Test
        @DisplayName("다른 회원 프로필 조회 실패 - 해당 회원 없음")
        void get_member_profile_fail_member_id_not_found() {
            // given
            Long memberId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMemberProfile(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateMember {

        @Test
        @DisplayName("회원 정보 수정 성공")
        void update_my_profile_success() {
            // given
            Long memberId = 1L;
            MemberInfoUpdateRequest request = MemberInfoUpdateRequest.builder()
                    .teamId(1L)
                    .nickname("newTester")
                    .aboutMe("hello")
                    .memberId(1L)
                    .build();
            MultipartFile image = createFile(MediaType.IMAGE_JPEG_VALUE);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.existsByNickname(request.getNickname()))
                    .willReturn(false);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            MyProfileResponse response = memberService.updateMyProfile(image, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getNickname()).isEqualTo("newTester");
            assertThat(response.getAboutMe()).isEqualTo("hello");
            assertThat(response.getTeamName()).isEqualTo(TeamInfo.getById(1L).shortName);

            verify(memberRepository).findById(member.getId());
            verify(memberRepository).existsByNickname(request.getNickname());
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("회원 정보 수정 실패 - 회원이 존재하지 않는 경우")
        void update_my_profile_fail_member_not_found() {
            // given
            Long memberId = 999L;  // 존재하지 않는 회원 ID
            MemberInfoUpdateRequest request = MemberInfoUpdateRequest.builder()
                    .teamId(1L)
                    .nickname("newTester")
                    .aboutMe("hello")
                    .memberId(memberId)
                    .build();
            MultipartFile image = createFile(MediaType.IMAGE_JPEG_VALUE);

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateMyProfile(image, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            verify(memberRepository).findById(memberId);
        }

        @Test
        @DisplayName("회원 정보 수정 실패 - 닉네임이 이미 존재하는 경우")
        void update_my_profile_fail_nickname_already_used() {
            // given
            Long memberId = 1L;
            MemberInfoUpdateRequest request = MemberInfoUpdateRequest.builder()
                    .teamId(1L)
                    .nickname("newTester")  // 이미 존재하는 닉네임
                    .aboutMe("hello")
                    .memberId(memberId)
                    .build();
            MultipartFile image = createFile(MediaType.IMAGE_JPEG_VALUE);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.existsByNickname(request.getNickname())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.updateMyProfile(image, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.ALREADY_USED_NICKNAME.getMessage());

            verify(memberRepository).findById(memberId);
            verify(memberRepository).existsByNickname(request.getNickname());
        }

        @Test
        @DisplayName("회원 정보 수정 실패 - 잘못된 팀 ID인 경우")
        void update_my_profile_fail_invalid_team_id() {
            // given
            Long memberId = 1L;
            MemberInfoUpdateRequest request = MemberInfoUpdateRequest.builder()
                    .teamId(999L)  // 존재하지 않는 팀 ID
                    .nickname("newTester")
                    .aboutMe("hello")
                    .memberId(memberId)
                    .build();
            MultipartFile image = createFile(MediaType.IMAGE_JPEG_VALUE);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> memberService.updateMyProfile(image, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.TEAM_NOT_FOUND.getMessage());

            verify(memberRepository).findById(memberId);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class DeleteMember {

        @Test
        @DisplayName("회원 탈퇴 성공")
        void delete_member_success() {
            // given
            Long memberId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            memberService.deleteMember(memberId);

            // then
            verify(memberRepository).findById(memberId);
            verify(memberRepository).deleteById(memberId);
        }

        @Test
        @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원")
        void delete_member_fail_not_exists_member() {
            // given
            Long memberId = 999L;

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.deleteMember(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            verify(memberRepository).findById(memberId);
        }
    }

    @Nested
    @DisplayName("회원 로그인")
    class LoginMember {

        @Test
        @DisplayName("회원 로그인 성공")
        void login_member_success() {
            // given
            String email = "test@example.com";
            MemberLoginRequest request = MemberLoginRequest.builder()
                    .email("test@example.com")
                    .build();

            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

            // when
            MemberLoginResponse response = memberService.loginByEmail(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getNickname()).isEqualTo("tester");
            assertThat(response.getAge()).isEqualTo(30);
            assertThat(response.getTeamId()).isEqualTo(1L);

            verify(memberRepository).findByEmail(member.getEmail());
        }

        @Test
        @DisplayName("회원 로그인 실패 - 존재하지 않는 이메일")
        void login_member_fail_non_exists_email() {
            // given
            String email = "test10@example.com";
            MemberLoginRequest request = MemberLoginRequest.builder()
                    .email("test10@example.com")
                    .build();

            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.loginByEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_EMAIL.getMessage());

            verify(memberRepository).findByEmail(email);
        }
    }
}