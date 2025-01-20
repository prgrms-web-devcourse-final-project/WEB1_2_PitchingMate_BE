package com.example.mate.domain.batch.config;

import com.example.mate.domain.batch.job.CrawlingQuartzJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfiguration {

    @Bean
    public JobDetail crawlingJobDetail() {
        return JobBuilder.newJob(CrawlingQuartzJob.class)
                .withIdentity("crawlingJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger morningTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(crawlingJobDetail())
                .withIdentity("morningTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 ? * *") // 매일 오전 9시
                        .inTimeZone(TimeZone.getTimeZone("Asia/Seoul")))
                .build();
    }

    @Bean
    public Trigger eveningTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(crawlingJobDetail())
                .withIdentity("eveningTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 22 ? * *")  // 매일 오후 22시
                        .inTimeZone(TimeZone.getTimeZone("Asia/Seoul")))
                .build();
    }
}