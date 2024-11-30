package com.example.mate.domain.member.service;

import static com.example.mate.common.error.ErrorCode.ALREADY_FOLLOWED_MEMBER;
import static com.example.mate.common.error.ErrorCode.ALREADY_UNFOLLOWED_MEMBER;
import static com.example.mate.common.error.ErrorCode.FOLLOWER_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.UNFOLLOWER_NOT_FOUND_BY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member follower;
    private Member following;

    @BeforeEach
    void setUp() {
        createTestMember();
    }

    private void createTestMember() {
        follower = Member.builder()
                .id(1L)
                .name("홍길동")
                .teamId(1L)
                .email("tester1@example.com")
                .nickname("tester1")
                .build();
        following = Member.builder()
                .id(2L)
                .name("김영희")
                .teamId(2L)
                .email("tester2@example.com")
                .nickname("tester2")
                .build();
    }

    private Follow createTestFollow() {
        return Follow.builder()
                .id(1L)
                .follower(follower)
                .following(following).build();
    }

    private Page<Member> createTestMemberPage() {
        createTestMember();
        return new PageImpl<>(List.of(follower, following));
    }

    @Nested
    @DisplayName("팔로우")
    class Following {

        @Test
        @DisplayName("팔로우 성공")
        void follow_member_success() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            Follow follow = createTestFollow();

            given(memberRepository.findById(followerId))
                    .willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId))
                    .willReturn(Optional.of(following));
            given(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId))
                    .willReturn(false);
            given(followRepository.save(any(Follow.class)))
                    .willReturn(follow);

            // when
            followService.follow(followerId, followingId);

            // then
            verify(memberRepository, times(1)).findById(followerId);
            verify(memberRepository, times(1)).findById(followingId);
            verify(followRepository).existsByFollowerIdAndFollowingId(followerId, followingId);
            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("팔로우 실패 - 이미 팔로우한 회원을 다시 팔로우하려는 경우 예외 발생")
        void follow_member_already_followed() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            given(memberRepository.findById(followerId))
                    .willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId))
                    .willReturn(Optional.of(following));
            given(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId))
                    .willReturn(true); // 이미 팔로우한 상태

            // when & then
            assertThatThrownBy(() -> followService.follow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ALREADY_FOLLOWED_MEMBER);

            verify(memberRepository, times(1)).findById(followerId);
            verify(memberRepository, times(1)).findById(followingId);
            verify(followRepository, never()).save(any());

        }

        @Test
        @DisplayName("팔로우 실패 - 존재하지 않는 팔로워 또는 팔로잉을 팔로우하려는 경우 예외 발생")
        void follow_member_not_found() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            // 팔로워가 존재하지 않는 경우
            given(memberRepository.findById(followerId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.follow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", FOLLOWER_NOT_FOUND_BY_ID);

            // 팔로워 조회는 한 번 호출되었고, 팔로잉은 조회되지 않음
            verify(memberRepository, times(1)).findById(followerId);
            verify(memberRepository, never()).findById(followingId);
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
            verify(followRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("언팔로우")
    class Unfollowing {

        @Test
        @DisplayName("언팔로우 성공")
        void unfollow_member_success() {
            // given
            Long unfollowerId = 1L;
            Long unfollowingId = 2L;
            Follow follow = createTestFollow();

            given(memberRepository.findById(unfollowerId))
                    .willReturn(Optional.of(follower));
            given(memberRepository.findById(unfollowingId))
                    .willReturn(Optional.of(following));
            given(followRepository.existsByFollowerIdAndFollowingId(unfollowerId, unfollowingId))
                    .willReturn(true); // 팔로우 관계가 존재

            // when
            followService.unfollow(unfollowerId, unfollowingId);

            // then
            verify(memberRepository, times(1)).findById(unfollowerId);
            verify(memberRepository, times(1)).findById(unfollowingId);
            verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(unfollowerId, unfollowingId);
            verify(followRepository, times(1)).deleteByFollowerIdAndFollowingId(unfollowerId, unfollowingId);
        }

        @Test
        @DisplayName("언팔로우 실패 - 이미 언팔로우한 회원을 다시 언팔로우하려는 경우 예외 발생")
        void unfollow_member_already_unfollowed() {
            // given
            Long unfollowerId = 1L;
            Long unfollowingId = 2L;

            given(memberRepository.findById(unfollowerId))
                    .willReturn(Optional.of(follower));
            given(memberRepository.findById(unfollowingId))
                    .willReturn(Optional.of(following));
            given(followRepository.existsByFollowerIdAndFollowingId(unfollowerId, unfollowingId))
                    .willReturn(false); // 이미 언팔로우된 상태

            // when & then
            assertThatThrownBy(() -> followService.unfollow(unfollowerId, unfollowingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ALREADY_UNFOLLOWED_MEMBER);

            verify(memberRepository, times(1)).findById(unfollowerId);
            verify(memberRepository, times(1)).findById(unfollowingId);
            verify(followRepository, never()).deleteByFollowerIdAndFollowingId(any(), any());
        }

        @Test
        @DisplayName("언팔로우 실패 - 존재하지 않는 언팔로워 또는 언팔로잉을 언팔로우하려는 경우 예외 발생")
        void unfollow_member_not_found() {
            // given
            Long unfollowerId = 1L;
            Long unfollowingId = 2L;

            // 언팔로워가 존재하지 않는 경우
            given(memberRepository.findById(unfollowerId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.unfollow(unfollowerId, unfollowingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UNFOLLOWER_NOT_FOUND_BY_ID);

            // 언팔로워 조회는 한 번 호출되었고, 언팔로잉은 조회되지 않음
            verify(memberRepository, times(1)).findById(unfollowerId);
            verify(memberRepository, never()).findById(unfollowingId);
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
            verify(followRepository, never()).deleteByFollowerIdAndFollowingId(any(), any());
        }
    }

    @Nested
    @DisplayName("팔로우 리스트 페이징")
    class FollowingPage {

        @Test
        @DisplayName("팔로우 리스트 페이징 성공")
        void get_followings_page_success() {
            // given
            Long memberId = 1L;
            int pageNumber = 1;
            int pageSize = 2;

            given(memberRepository.findById(memberId)).willReturn(Optional.of(follower));

            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Direction.DESC, "id"));
            Page<Member> membersPage = createTestMemberPage();

            given(followRepository.findFollowingsByFollowerId(eq(memberId), eq(pageable)))
                    .willReturn(membersPage);

            // when
            PageResponse<MemberSummaryResponse> response = followService.getFollowingsPage(memberId, pageNumber,
                    pageSize);

            // then
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).getNickname()).isEqualTo("tester1");
            assertThat(response.getContent().get(1).getNickname()).isEqualTo("tester2");

            verify(memberRepository, times(1)).findById(memberId);
            verify(followRepository, times(1))
                    .findFollowingsByFollowerId(memberId, pageable);
        }

        @Test
        @DisplayName("팔로우 리스트 페이징 실패 - 회원 없음")
        void get_followings_page_member_not_found() {
            // given
            Long memberId = 999L;  // 존재하지 않는 회원 ID
            int pageNumber = 1;
            int pageSize = 2;

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.getFollowingsPage(memberId, pageNumber, pageSize))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            verify(memberRepository, times(1)).findById(memberId);
        }
    }
}