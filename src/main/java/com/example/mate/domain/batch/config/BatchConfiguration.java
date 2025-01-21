package com.example.mate.domain.batch.config;

import com.example.mate.domain.batch.step.processor.CrawlingReader;
import com.example.mate.domain.batch.step.reader.CrawlingProcessor;
import com.example.mate.domain.batch.step.writer.CrawlingWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {

    @Bean
    public Job crawlingJob(JobRepository jobRepository, Step crawlingStep) {
        return new JobBuilder("crawlingJob", jobRepository)
                .start(crawlingStep)
                .build();
    }

    @Bean
    public Step crawlingStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             CrawlingReader reader,
                             CrawlingProcessor processor,
                             CrawlingWriter writer) {
        return new StepBuilder("crawlingStep", jobRepository)
                .<String, String>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}