package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    // 특정 회원 팔로우 - 해당 회원 팔로우 되어있는지 확인한 뒤 팔로우
    public void follow(Long followerId, Long followingId) {
        Map<String, Member> members = isValidMember(followerId, followingId);
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            followRepository.save(createFollow(members.get("follower"), members.get("following")));
        } else {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWED_MEMBER);
        }
    }

    private Map<String, Member> isValidMember(Long followerId, Long followingId) {
        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWER_NOT_FOUND_BY_ID));
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWING_NOT_FOUND_BY_ID));
        return Map.of("follower", follower, "following", following);
    }

    private Follow createFollow(Member follower, Member following) {
        return Follow.builder().follower(follower).following(following).build();
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    // 특정 회원의 팔로잉 수 카운트
    public int getFollowCount(Long memberId) {
        return followRepository.countByFollowerId(memberId);
    }

    // 특정 회원의 팔로워 수 카운트
    public int getFollowerCount(Long memberId) {
        return followRepository.countByFollowingId(memberId);
    }


}
