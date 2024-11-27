package com.example.mate.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.auth.config.OAuthConfig;
import com.example.mate.domain.auth.dto.response.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class NaverAuthServiceTest {

    @InjectMocks
    private NaverAuthService naverAuthService;

    @Mock
    private OAuthConfig oAuthConfig;

    @Mock
    private RestTemplate restTemplate;

    @Test
    @DisplayName("네이버 로그인 연결 URL 생성")
    public void getAuthUrl_Success() {
        // when
        String authUrl = naverAuthService.getAuthUrl();

        // then
        assertTrue(authUrl.startsWith("https://nid.naver.com/oauth2.0/authorize"));
        assertTrue(authUrl.contains("client_id=" + oAuthConfig.getNaverClientId()));
        assertTrue(authUrl.contains("redirect_uri=" + oAuthConfig.getNaverRedirectUri()));
        assertTrue(authUrl.contains("response_type=code"));
    }

    @Test
    @DisplayName("네이버 로그인 API와의 상호작용이 성공할 때, 올바른 로그인 응답을 반환")
    public void authenticateNaver_Success() {
        // given : 토큰 응답
        String tokenResponse = "{\"access_token\":\"access_token_value\",\"refresh_token\":\"refresh_token_value\"}";
        when(restTemplate.exchange(
                eq("https://nid.naver.com/oauth2.0/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        // given : 네이버 프로필 응답
        String profileResponse = "{\"response\":{\"name\":\"홍길동\",\"nickname\":\"tester\",\"email\":\"tester@naver.com\",\"gender\":\"M\",\"birthyear\":\"2000\"}}";
        when(restTemplate.exchange(
                eq("https://openapi.naver.com/v1/nid/me"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(profileResponse));

        // when
        LoginResponse loginResponse = naverAuthService.authenticateNaver("code", "state");

        // then
        assertEquals("access_token_value", loginResponse.getAccessToken());
        assertEquals("refresh_token_value", loginResponse.getRefreshToken());
        assertEquals("홍길동", loginResponse.getNaverProfileResponse().getName());
        assertEquals("tester@naver.com", loginResponse.getNaverProfileResponse().getEmail());
        assertEquals("M", loginResponse.getNaverProfileResponse().getGender());
        assertEquals("Bearer", loginResponse.getGrantType());
        assertTrue(loginResponse.getIsNewMember());
    }

    @Test
    @DisplayName("네이버 로그인 과정에서 토큰 발급에 실패할 경우, CustomException 예외 발생")
    public void authenticateNaver_FailToGetTokens() {
        // given : 잘못된 토큰 응답
        when(restTemplate.exchange(
                eq("https://nid.naver.com/oauth2.0/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new CustomException(ErrorCode.AUTH_BAD_REQUEST));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> naverAuthService.authenticateNaver("code", "state"));
        assertEquals(ErrorCode.AUTH_BAD_REQUEST, exception.getErrorCode());
    }

    @Test
    @DisplayName("네이버 로그인 API에서 사용자 정보 요청이 실패할 경우 CustomException 예외 발생")
    public void authenticateNaver_FailToGetNaverLoginProfile() {
        // given : 토큰 정상 발급
        String tokenResponse = "{\"access_token\":\"access_token_value\",\"refresh_token\":\"refresh_token_value\"}";
        when(restTemplate.exchange(
                eq("https://nid.naver.com/oauth2.0/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        // given : 사용자 정보 요청 실패
        when(restTemplate.exchange(
                eq("https://openapi.naver.com/v1/nid/me"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new CustomException(
                        ErrorCode.AUTH_BAD_REQUEST));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                naverAuthService.authenticateNaver("code", "state"));
        assertEquals(ErrorCode.AUTH_BAD_REQUEST, exception.getErrorCode());
    }
}
