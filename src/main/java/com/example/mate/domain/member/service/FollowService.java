package com.example.mate.domain.member.service;

import com.example.mate.domain.member.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    // 특정 회원의 팔로잉 수 카운트
    public int getFollowCount(Long memberId) {
        return followRepository.countByFollowerId(memberId);
    }

    // 특정 회원의 팔로워 수 카운트
    public int getFollowerCount(Long memberId) {
        return followRepository.countByFollowingId(memberId);
    }
}
