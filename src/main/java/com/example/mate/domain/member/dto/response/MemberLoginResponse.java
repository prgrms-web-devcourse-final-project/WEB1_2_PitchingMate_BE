package com.example.mate.domain.member.dto.response;

import com.example.mate.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberLoginResponse {

    private final Long memberId;
    private final String grantType;
    private final String accessToken;
    private final String refreshToken;

    // TODO : 파라미터로 JwtToken 추가 및 토큰 매핑
    public static MemberLoginResponse from(Member member) {
        return MemberLoginResponse.builder()
                .memberId(member.getId())
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();
    }
}
