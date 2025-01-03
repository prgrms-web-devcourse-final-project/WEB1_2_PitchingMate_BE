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

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Configuration
@Slf4j
public class WebDriverConfig {

    private static final boolean IS_DOCKER = System.getenv().containsKey("CHROME_BIN");

    @Bean
    @Scope("prototype")
    public WebDriver chromeDriver() {
        log.info("Creating new Chrome WebDriver instance... Docker environment: {}", IS_DOCKER);
        try {
            ChromeOptions options = new ChromeOptions();

            if (IS_DOCKER) {
                // Docker 환경 설정
                log.info("Configuring for Docker environment");
                System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
                System.setProperty("chrome.binary", "/usr/bin/chromium-browser");
                options.setBinary("/usr/bin/chromium-browser");
            } else {
                // 로컬 환경 설정
                log.info("Configuring for local environment");
                WebDriverManager.chromedriver().setup();
            }

            // 공통 옵션 설정
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");

            // Docker 환경에서 하드웨어 가속화 비활성화 (필수)
            if (IS_DOCKER) {
                options.addArguments("--disable-gpu");
            }

            WebDriver driver = new ChromeDriver(options);
            log.info("ChromeDriver successfully initialized");
            return driver;

        } catch (Exception e) {
            log.error("Failed to initialize Chrome WebDriver: ", e);
            throw new CustomException(ErrorCode.WEBDRIVER_ERROR);
        }
    }
}