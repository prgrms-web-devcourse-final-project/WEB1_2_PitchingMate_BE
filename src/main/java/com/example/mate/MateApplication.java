package com.example.mate;

import com.example.mate.domain.auth.config.OAuthConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(OAuthConfig.class)
@SpringBootApplication
public class MateApplication {

    public static void main(String[] args) {
        SpringApplication.run(MateApplication.class, args);
    }

}
