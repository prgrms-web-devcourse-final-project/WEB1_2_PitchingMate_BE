package com.example.mate.common.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);                            // 기본 스레드 풀 크기
        executor.setMaxPoolSize(10);                            // 최대 스레드 풀 크기
        executor.setQueueCapacity(100);                         // 대기 큐의 크기
        executor.setThreadNamePrefix("Event Executor-");        // 스레드 이름

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAllowCoreThreadTimeOut(true);               // 유휴 스레드 제거 활성화
        executor.setKeepAliveSeconds(60);                       // 유휴 스레드 유지 시간

        executor.setWaitForTasksToCompleteOnShutdown(true);     // shutdown 시 남은 queue 작업 처리
        executor.setAwaitTerminationSeconds(60);	            // 최대 60초 대기

        executor.initialize();
        return executor;
    }
}
