package com.example.mate.domain.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {

    private final String grantType;
    private final String accessToken;
    private final String refreshToken;
    private final Boolean isNewMember;  // TODO : 2024/11/27 - 로그인이 회원가입까지 연결되면, 회원 조회 결과에 따라 바뀔 예정
    private final NaverProfileResponse naverProfileResponse;    // TODO : 2024/11/27 - 필요한 사용자 정보만 반환 예정 (nickname, aboutMe, teamName)
}
