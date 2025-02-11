package com.example.mate.domain.auth.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.domain.auth.dto.response.LoginResponse;
import com.example.mate.domain.auth.dto.response.NaverProfileResponse;
import com.example.mate.domain.auth.service.NaverAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

//@WebMvcTest(AuthController.class)   // webmvctest는 해당 클래스만 가져오므로, 시큐리티 설정이 동작을 안한다. 스프링 부트 테스트로.
@SpringBootTest
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NaverAuthService naverAuthService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("네이버 소셜 로그인 요청 시, 네이버 인증 페이지로 리다이렉트 성공")
    void connectNaver_Success() throws Exception {
        // given
        String expectedAuthUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=testClientId&redirect_uri=testRedirectUri&response_type=code&state=STATE_STRING";
        when(naverAuthService.getAuthUrl("state")).thenReturn(expectedAuthUrl);

        // when & then
        mockMvc.perform(get("/api/auth/connect/naver")
                        .param("state", "state"))
                .andExpect(status().is3xxRedirection()) // 3xx 리다이렉션 응답 상태
                .andExpect(redirectedUrl(expectedAuthUrl)); // 리다이렉트 URL 확인
    }

    @Test
    @DisplayName("인증 페이지에서 로그인한 뒤, 생성한 토큰과 네이버 사용자 정보 반환")
    void loginByNaver_Success() throws Exception {
        // given
        String code = "testCode";
        String state = "testState";
        LoginResponse mockResponse = LoginResponse.builder()
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .isNewMember(true)
                .naverProfileResponse(NaverProfileResponse.builder()
                        .name("홍길동")
                        .email("tester@naver.com")
                        .gender("M")
                        .birthyear("2000")
                        .build())
                .build();
        when(naverAuthService.authenticateNaver(code, state)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/auth/login/naver")
                        .param("code", code)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grantType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.isNewMember").value(true))
                .andExpect(jsonPath("$.naverProfileResponse.name").value("홍길동"));
    }
}
