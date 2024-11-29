package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final GoodsPostRepository goodsPostRepository;

    // 다른 회원 프로필 조회
    public MemberProfileResponse getMemberProfile(Long memberId) {
        Member member = findByMemberId(memberId);
        int followCount = followRepository.countByFollowerId(memberId);
        int followerCount = followRepository.countByFollowingId(memberId);
        int goodsSoldCount = goodsPostRepository.countGoodsPostsBySellerIdAndStatus(memberId, Status.CLOSED);
        return MemberProfileResponse.of(member, followCount, followerCount, goodsSoldCount);
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }
}
