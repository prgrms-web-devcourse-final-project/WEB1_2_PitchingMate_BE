package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.jwt.JwtToken;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.request.MemberLoginRequest;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final JwtUtil jwtUtil;

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

    public JwtToken loginByEmail(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_EMAIL));
        return makeToken(member);
    }

    // JWT 토큰 생성
    private JwtToken makeToken(Member member) {
        Map<String, Object> payloadMap = member.getPayload();
        String accessToken = jwtUtil.createToken(payloadMap, 60); // 60분 유효
        String refreshToken = jwtUtil.createToken(Map.of("memberId", member.getId()), 60 * 24 * 7); // 7일 유효
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
