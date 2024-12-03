package com.example.mate.domain.auth.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.auth.config.OAuthConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties(OAuthConfig.class)
public class NaverAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuthConfig oAuthConfig;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Mock 서버 설정: 네이버 토큰 발급 API
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\": \"accessToken\", \"refresh_token\": \"refreshToken\"}",
                        MediaType.APPLICATION_JSON));

        // Mock 서버 설정: 네이버 사용자 정보 API
        mockServer.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{ \"response\": { " +
                                "\"name\": \"홍길동\", " +
                                "\"nickname\": \"오리\", " +
                                "\"email\": \"tester222\", " +
                                "\"gender\": \"M\", " +
                                "\"birthyear\": \"2000\" " +
                                "} }",
                        MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("네이버 소셜 로그인 요청 시, 네이버 인증 페이지로 리다이렉트 성공")
    void connectNaver_Success() throws Exception {
        // given
        String expectedUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?client_id=" + oAuthConfig.getNaverClientId()
                + "&redirect_uri=" + oAuthConfig.getNaverRedirectUri()
                + "&response_type=code"
                + "&state=STATE_STRING";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/connect/naver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", is(expectedUrl)));
    }

    @Test
    @DisplayName("네이버 소셜 로그인 콜백 요청 시, 올바른 응답 반환 성공")
    void loginByNaver_Success() throws Exception {
        // given
        String code = "sampleCode";
        String state = "sampleState";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/login/naver")
                        .param("code", code)
                        .param("state", state))
                .andExpect(status().isOk()) // 응답 상태 확인
                .andExpect(jsonPath("$.grantType", is("Bearer"))) // 응답 내용 확인
                .andExpect(jsonPath("$.accessToken", is("accessToken")))
                .andExpect(jsonPath("$.refreshToken", is("refreshToken")))
                .andExpect(jsonPath("$.isNewMember", is(true))) // 신규 회원 여부 확인
                .andExpect(jsonPath("$.naverProfileResponse.name", is("홍길동"))) // 사용자 이름 확인
                .andExpect(jsonPath("$.naverProfileResponse.email", is("tester222"))) // 이메일 확인
                .andExpect(jsonPath("$.naverProfileResponse.gender", is("M"))) // 성별 확인
                .andExpect(jsonPath("$.naverProfileResponse.birthyear", is("2000"))); // 생년 확인
    }
}