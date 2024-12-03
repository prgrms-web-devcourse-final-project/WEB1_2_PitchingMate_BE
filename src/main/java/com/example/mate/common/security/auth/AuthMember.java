package com.example.mate.common.security.auth;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.security.Principal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthMember implements Principal {

    private final String userId;

    @Getter
    private final Long memberId;    // memberId 반환

    // 사용자 ID 반환
    @Override
    public String getName() {
        return this.userId;
    }

    public Member validateAuthMember(MemberRepository memberRepository) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }
}
