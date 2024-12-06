package com.example.mate;

import com.example.mate.domain.auth.config.OAuthConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret_key=testsecretkey12345678901234567890",
        "oauth.naver.client-id=test_client_id",
        "oauth.naver.redirect-uri=http://localhost:8080/callback",
        "oauth.naver.client-secret=test_client_secret",
        "openweather.api.key=test_api_key"
})
@EnableConfigurationProperties(OAuthConfig.class)
class MateApplicationTests {

    @Test
    void contextLoads() {
    }

}
