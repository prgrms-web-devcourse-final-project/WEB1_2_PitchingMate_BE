package com.example.mate.domain.crawler.config;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@Slf4j
public class WebDriverConfig {
    @Bean
    @Scope("prototype")
    public WebDriver chromeDriver() {
        log.info("Creating new Chrome WebDriver instance...");
        try {
            WebDriverManager.chromedriver().setup();
            log.info("WebDriverManager setup completed");

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");

            return new ChromeDriver(options);
        } catch (Exception e) {
            log.error("Failed to initialize Chrome WebDriver: ", e);
            throw new CustomException(ErrorCode.WEBDRIVER_ERROR);
        }
    }
}