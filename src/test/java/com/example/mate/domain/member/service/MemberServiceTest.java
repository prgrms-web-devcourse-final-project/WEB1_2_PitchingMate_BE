package com.example.mate.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
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
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoodsPostRepository goodsPostRepository;

    @Mock
    private FollowRepository followRepository;

    private Member member;
    private GoodsPost goodsPost;

    @BeforeEach
    void setUp() {
        createTestMember();
        createTestGoodsPost();
    }

    private void createTestMember() {
        member = Member.builder()
                .id(1L)
                .name("홍길동")
                .teamId(1L)
                .email("test@example.com")
                .nickname("tester")
                .build();
    }

    private void createTestGoodsPost() {
        goodsPost = GoodsPost.builder()
                .id(1L)
                .seller(member)
                .status(Status.CLOSED)
                .build();
    }

    @Test
    @DisplayName("다른 회원 프로필 조회 - 성공")
    void get_member_profile_success() {
        // given
        Long memberId = 1L;
        int followCount = 10;
        int followerCount = 20;
        int goodsSoldCount = 1;

        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
        given(followRepository.countByFollowerId(memberId)).willReturn(followCount);
        given(followRepository.countByFollowingId(memberId)).willReturn(followerCount);
        given(goodsPostRepository.countGoodsPostsBySellerIdAndStatus(memberId, Status.CLOSED)).
                willReturn(goodsSoldCount);

        // when
        MemberProfileResponse result = memberService.getMemberProfile(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(member.getNickname());
        assertThat(result.getFollowingCount()).isEqualTo(followCount);
        assertThat(result.getFollowerCount()).isEqualTo(followerCount);
        assertThat(result.getGoodsSoldCount()).isEqualTo(goodsSoldCount);
    }

    @Test
    @DisplayName("회원 프로필 조회 - 실패 (해당 회원 없음)")
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