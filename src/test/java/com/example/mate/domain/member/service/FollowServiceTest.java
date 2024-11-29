package com.example.mate.domain.member.service;

import static com.example.mate.common.error.ErrorCode.ALREADY_FOLLOWED_MEMBER;
import static com.example.mate.common.error.ErrorCode.FOLLOWER_NOT_FOUND_BY_ID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    @DisplayName("다른 회원 팔로우 성공")
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
    @DisplayName("이미 팔로우한 회원을 다시 팔로우하려는 경우 예외 발생")
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
    @DisplayName("존재하지 않는 팔로워 또는 팔로잉을 팔로우하려는 경우 예외 발생")
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