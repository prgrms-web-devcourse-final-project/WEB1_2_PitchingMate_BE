package com.example.mate.domain.auth.controller;

import com.example.mate.global.jwt.JwtToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<JwtToken> loginByNaver(@RequestParam String code) {
        JwtToken jwtToken = JwtToken.builder()
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        return ResponseEntity.ok(jwtToken);
    }

    /*
    TODO : 2024/11/22 - 구글 소셜 로그인 및 회원가입
    1. 인가 코드를 통해 토큰 정보 요청
    2. 토큰을 통해 사용자 정보 요청
    3. 사용자 정보 조회 -> 회원가입 or 로그인
    4. 사용자 정보 Jwt 토큰으로 반환
    */
    @GetMapping("/login/google")
    public ResponseEntity<JwtToken> loginByGoogle(@RequestParam String code) {
        JwtToken jwtToken = JwtToken.builder()
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        return ResponseEntity.ok(jwtToken);
    }
}

