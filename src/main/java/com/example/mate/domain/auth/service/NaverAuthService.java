package com.example.mate.domain.auth.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.auth.config.OAuthConfig;
import com.example.mate.domain.auth.dto.response.LoginResponse;
import com.example.mate.domain.auth.dto.response.NaverProfileResponse;
import com.example.mate.domain.member.repository.MemberRepository;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final OAuthConfig oAuthConfig;
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;

    /**
     * 네이버 로그인 연결 URL을 생성
     *
     * @return 네이버 로그인 URL
     */
    public String getAuthUrl() {
        return "https://nid.naver.com/oauth2.0/authorize"
                + "?client_id=" + oAuthConfig.getNaverClientId()
                + "&redirect_uri=" + oAuthConfig.getNaverRedirectUri()
                + "&response_type=code"
                + "&state=STATE_STRING";
    }

    /**
     * 네이버 소셜 로그인을 처리
     *
     * @param code  네이버에서 받은 인증 코드
     * @param state CSRF 공격 방지를 위한 상태 값
     * @return 로그인 토큰 응답
     */
    public LoginResponse authenticateNaver(String code, String state) {
        // 1. 접근 토큰 발급 요청
        Map<String, String> tokens = getTokens(code, state);

        // 2. 사용자 정보 요청
        NaverProfileResponse profileResponse = getNaverProfile(tokens.get("accessToken"));

        // 3. 사용자 정보 기반 응답 생성
        return createLoginTokenResponse(tokens, profileResponse);
    }

    /**
     * 네이버 API에 접근 토큰을 요청
     *
     * @param code  네이버에서 받은 인증 코드
     * @param state CSRF 공격 방지를 위한 상태 값
     * @return 접근 토큰과 리프레시 토큰을 포함하는 맵
     */
    private Map<String, String> getTokens(String code, String state) {
        String requestUrl = "https://nid.naver.com/oauth2.0/token";

        MultiValueMap<String, String> params = createTokenRequestParams(code, state);
        String responseBody = sendPostRequest(requestUrl, params);

        JsonObject asJsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        return Map.of(
                "accessToken", asJsonObject.get("access_token").getAsString(),
                "refreshToken", asJsonObject.get("refresh_token").getAsString()
        );
    }

    /**
     * 접근 토큰 요청에 필요한 파라미터를 생성
     *
     * @param code  네이버에서 받은 인증 코드
     * @param state CSRF 공격 방지를 위한 상태 값
     * @return 요청 파라미터
     */
    private MultiValueMap<String, String> createTokenRequestParams(String code, String state) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", oAuthConfig.getNaverClientId());
        params.add("client_secret", oAuthConfig.getNaverClientSecret());
        params.add("code", code);
        params.add("state", state);
        return params;
    }

    /**
     * 토큰을 받기 위해 POST 요청을 보내고 응답을 반환
     *
     * @param url    요청을 보낼 URL
     * @param params 요청 파라미터
     * @return 응답 본문
     */
    private String sendPostRequest(String url, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.AUTH_BAD_REQUEST);
        }
        return response.getBody();
    }

    /**
     * 사용자 정보를 요청하기 위해 POST 요청을 보내고 응답을 반환
     *
     * @param url     요청을 보낼 URL
     * @param request 요청 엔티티
     * @return 응답 본문
     */
    private String sendPostRequest(String url, HttpEntity<MultiValueMap<String, String>> request) {
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.AUTH_BAD_REQUEST);
        }
        return response.getBody();
    }

    /**
     * 네이버 API를 통해 사용자 정보를 요청
     *
     * @param accessToken 접근 토큰
     * @return 사용자 프로필 정보
     */
    private NaverProfileResponse getNaverProfile(String accessToken) {
        String reqUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<MultiValueMap<String, String>> naverProfileRequest = new HttpEntity<>(headers);

        String responseBody = sendPostRequest(reqUrl, naverProfileRequest);
        return NaverProfileResponse.fromJson(responseBody);
    }

    /**
     * 로그인 토큰 응답을 생성
     *
     * @param tokens   접근 토큰과 리프레시 토큰
     * @param userInfo 사용자 프로필 정보
     * @return 로그인 토큰 응답
     */
    private LoginResponse createLoginTokenResponse(Map<String, String> tokens, NaverProfileResponse userInfo) {
        return LoginResponse.builder()
                .grantType("Bearer")
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .isNewMember(isNewMemberByEmail(userInfo.getEmail()))
                .naverProfileResponse(userInfo)
                .build();
    }

    // email을 통해 새로운 회원인지 검증
    private boolean isNewMemberByEmail(String email) {
        return !memberRepository.existsByEmail(email);
    }
}
