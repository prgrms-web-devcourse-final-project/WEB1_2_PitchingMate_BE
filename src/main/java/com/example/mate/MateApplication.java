package com.example.mate;

import com.example.mate.domain.auth.config.OAuthConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(OAuthConfig.class)
@SpringBootApplication
public class MateApplication {

    public static void main(String[] args) {
        SpringApplication.run(MateApplication.class, args);
    }

}
