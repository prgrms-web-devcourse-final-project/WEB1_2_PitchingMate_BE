package com.example.mate.domain.member.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginTokenResponse {

    private final String grantType;
    private final String accessToken;
    private final String refreshToken;
    private final Boolean isNewMember;

    // TODO: 사용자 정보 추가 예정
}
