package com.example.mate.domain.auth.controller;

import com.example.mate.common.jwt.JwtToken;
import com.example.mate.domain.auth.dto.response.LoginResponse;
import com.example.mate.domain.auth.service.NaverAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth Controller", description = "소셜 로그인 관련 API")
public class AuthController {

    private final NaverAuthService naverAuthService;

    /**
     * 네이버 소셜 로그인 연결 : 네이버 인증 페이지로의 리다이렉트
     */
    @Operation(summary = "네이버 소셜 인증 페이지 리다이렉트")
    @GetMapping("/connect/naver")
    public RedirectView connectNaver(
            @Parameter(description = "네이버 로그인 요청 state") @RequestParam String state) {
        return new RedirectView(naverAuthService.getAuthUrl(state));
    }

    /**
     * 네이버 소셜 로그인 콜백 : 인증 페이지에서 로그인한 뒤, 네이버 사용자 정보와 로그인 토큰을 함께 반환
     */
    @Operation(summary = "네이버 소셜 로그인 콜백")
    @GetMapping("/login/naver")
    public ResponseEntity<LoginResponse> loginByNaver(@RequestParam @NotEmpty String code,
                                                      @RequestParam String state) {
        // TODO : 2024/11/27 - 로그인 회원 조회 결과에 따라 회원가입/로그인완료 연결 필요
        return ResponseEntity.ok(naverAuthService.authenticateNaver(code, state));
    }

    /*
    TODO : 2024/11/22 - 구글 소셜 로그인 및 회원가입
    1. 인가 코드를 통해 토큰 정보 요청
    2. 토큰을 통해 사용자 정보 요청
    3. 사용자 정보 조회 -> 회원가입 or 로그인
    4. 사용자 정보 Jwt 토큰으로 반환
    */
    @GetMapping("/login/google")
    public ResponseEntity<LoginResponse> loginByGoogle(@RequestParam String code) {
        LoginResponse response = LoginResponse.builder()
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
