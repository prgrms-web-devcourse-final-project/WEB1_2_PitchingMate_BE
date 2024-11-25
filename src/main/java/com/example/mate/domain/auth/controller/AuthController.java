package com.example.mate.domain.auth.controller;

import com.example.mate.domain.member.dto.response.LoginTokenResponse;
import com.example.mate.global.jwt.JwtToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /*
    TODO : 2024/11/22 - 네이버 소셜 로그인 및 회원가입
    1. 인가 코드를 통해 토큰 정보 요청
    2. 토큰을 통해 사용자 정보 요청
    3. 사용자 정보 조회 -> 회원가입 or 로그인
    4. 사용자 정보 Jwt 토큰으로 반환
    */
    @GetMapping("/login/naver")
    public ResponseEntity<LoginTokenResponse> loginByNaver(@RequestParam String code) {
        LoginTokenResponse response = LoginTokenResponse.builder()
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .isNewMember(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /*
    TODO : 2024/11/22 - 구글 소셜 로그인 및 회원가입
    1. 인가 코드를 통해 토큰 정보 요청
    2. 토큰을 통해 사용자 정보 요청
    3. 사용자 정보 조회 -> 회원가입 or 로그인
    4. 사용자 정보 Jwt 토큰으로 반환
    */
    @GetMapping("/login/google")
    public ResponseEntity<LoginTokenResponse> loginByGoogle(@RequestParam String code) {
        LoginTokenResponse response = LoginTokenResponse.builder()
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .isNewMember(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /*
    TODO : 2024/11/23 - Jwt 토큰 재발급
    1. refresh 토큰 유효성 검증
    2. Jwt 토큰 재발급
    redis 를 통한 blackList 관리는 리팩토링 단계에 추가 예정
    */
    @PostMapping("/reissue-token")
    public ResponseEntity<JwtToken> reissueToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
        JwtToken jwtToken = JwtToken.builder()
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        return ResponseEntity.ok(jwtToken);
    }
}

