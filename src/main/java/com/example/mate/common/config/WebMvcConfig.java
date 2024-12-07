package com.example.mate.common.config;

import com.example.mate.common.validator.ValidPageableArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ValidPageableArgumentResolver validPageableArgumentResolver;

    public WebMvcConfig(ValidPageableArgumentResolver validPageableArgumentResolver) {
        this.validPageableArgumentResolver = validPageableArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(validPageableArgumentResolver);
    }
}
