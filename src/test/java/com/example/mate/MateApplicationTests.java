package com.example.mate;

import com.example.mate.domain.auth.config.OAuthConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnableConfigurationProperties(OAuthConfig.class)
@ActiveProfiles("test")
class MateApplicationTests {

    @Test
    void contextLoads() {
    }

}
