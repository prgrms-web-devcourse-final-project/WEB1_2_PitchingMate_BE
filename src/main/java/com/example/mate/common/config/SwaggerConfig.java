package com.example.mate.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Baseball Match API")
                .version("v1.0.0")
                .description("야구 경기 정보 제공 API 문서");

        return new OpenAPI()
                .info(info)
                .servers(List.of(new Server().url("http://localhost:8000")));
    }
}