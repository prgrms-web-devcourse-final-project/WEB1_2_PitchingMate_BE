package com.example.mate.common.security.auth;

import java.security.Principal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserPrincipal implements Principal {

    private final String userId;

    @Getter
    private final Long memberId;    // memberId 반환

    // 사용자 ID 반환
    @Override
    public String getName() {
        return this.userId;
    }
}
