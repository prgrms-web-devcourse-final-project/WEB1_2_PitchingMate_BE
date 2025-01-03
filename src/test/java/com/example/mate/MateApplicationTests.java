package com.example.mate;

import com.example.mate.domain.auth.config.OAuthConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableConfigurationProperties(OAuthConfig.class)
class MateApplicationTests {

    @Test
    void contextLoads() {
    }

}
