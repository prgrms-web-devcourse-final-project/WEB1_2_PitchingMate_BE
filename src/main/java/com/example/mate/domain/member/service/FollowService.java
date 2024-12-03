package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    // 특정 회원 팔로우 - 해당 회원 팔로우 되어있는지 확인한 뒤 팔로우
    public void follow(Long followerId, Long followingId) {
        Map<String, Member> members = isValidMemberFollow(followerId, followingId);
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            followRepository.save(createFollow(members.get("follower"), members.get("following")));
        } else {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWED_MEMBER);
        }
    }

    // 특정 회원 언팔로우 - 해당 회원 팔로우 되어있는지 확인한 뒤 언팔로우
    public void unfollow(Long unfollowerId, Long unfollowingId) {
        Map<String, Member> members = isValidMemberUnfollow(unfollowerId, unfollowingId);
        if (followRepository.existsByFollowerIdAndFollowingId(unfollowerId, unfollowingId)) {
            followRepository.deleteByFollowerIdAndFollowingId(unfollowerId, unfollowingId);
        } else {
            throw new CustomException(ErrorCode.ALREADY_UNFOLLOWED_MEMBER);
        }
    }

    // 특정 회원의 팔로우 리스트 페이징 조회
    public PageResponse<MemberSummaryResponse> getFollowingsPage(Long memberId, Pageable pageable) {
        findByMemberId(memberId); // 회원 존재 검증

        // 해당 회원이 팔로우하는 리스트 최신 팔로우 순으로 페이징
        Page<Member> followingsPage = followRepository.findFollowingsByFollowerId(memberId, pageable);

        // MemberSummaryResponse 변환
        List<MemberSummaryResponse> content = followingsPage.getContent().stream()
                .map(MemberSummaryResponse::from)
                .toList();

        return PageResponse.<MemberSummaryResponse>builder()
                .content(content)
                .totalPages(followingsPage.getTotalPages())
                .totalElements(followingsPage.getTotalElements())
                .hasNext(followingsPage.hasNext())
                .pageNumber(followingsPage.getNumber())
                .pageSize(followingsPage.getSize())
                .build();
    }

    // 특정 회원의 팔로워 리스트 페이징 조회
    public PageResponse<MemberSummaryResponse> getFollowersPage(Long memberId, Pageable pageable) {
        findByMemberId(memberId);

        // 해당 회원이 팔로우하는 리스트 최신 팔로우 순으로 페이징
        Page<Member> followingsPage = followRepository.findFollowersByFollowingId(memberId, pageable);

        // MemberSummaryResponse 변환
        List<MemberSummaryResponse> content = followingsPage.getContent().stream()
                .map(MemberSummaryResponse::from)
                .toList();

        return PageResponse.<MemberSummaryResponse>builder()
                .content(content)
                .totalPages(followingsPage.getTotalPages())
                .totalElements(followingsPage.getTotalElements())
                .hasNext(followingsPage.hasNext())
                .pageNumber(followingsPage.getNumber())
                .pageSize(followingsPage.getSize())
                .build();
    }

    private Map<String, Member> isValidMemberFollow(Long followerId, Long followingId) {
        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWER_NOT_FOUND_BY_ID));
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWING_NOT_FOUND_BY_ID));
        return Map.of("follower", follower, "following", following);
    }

    private Map<String, Member> isValidMemberUnfollow(Long unfollowerId, Long unfollowingId) {
        Member follower = memberRepository.findById(unfollowerId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNFOLLOWER_NOT_FOUND_BY_ID));
        Member following = memberRepository.findById(unfollowingId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNFOLLOWING_NOT_FOUND_BY_ID));
        return Map.of("unfollower", follower, "unfollowing", following);
    }


    private Follow createFollow(Member follower, Member following) {
        return Follow.builder().follower(follower).following(following).build();
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }
}
