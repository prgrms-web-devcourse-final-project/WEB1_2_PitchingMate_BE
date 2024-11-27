package com.example.mate.domain.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "oauth")
public class OAuthConfig {

    private String naverClientId;
    private String naverRedirectUri;
    private String naverClientSecret;
}
