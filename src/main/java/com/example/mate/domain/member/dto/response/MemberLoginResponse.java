package com.example.mate.domain.member.dto.response;

import com.example.mate.common.jwt.JwtToken;
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

    public static MemberLoginResponse from(Member member, JwtToken jwtToken) {
        return MemberLoginResponse.builder()
                .memberId(member.getId())
                .grantType(jwtToken.getGrantType())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }
}
