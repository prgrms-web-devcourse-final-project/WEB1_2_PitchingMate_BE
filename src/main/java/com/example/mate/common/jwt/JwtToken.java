package com.example.mate.common.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtToken {

    private final String grantType;
    private final String accessToken;
    private final String refreshToken;
}