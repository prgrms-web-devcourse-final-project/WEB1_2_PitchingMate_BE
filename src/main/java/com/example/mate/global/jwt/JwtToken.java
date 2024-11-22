package com.example.mate.global.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtToken {

    final String grantType;
    final String accessToken;
    final String refreshToken;
}