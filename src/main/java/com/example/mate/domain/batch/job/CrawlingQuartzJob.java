package com.example.mate.domain.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingQuartzJob extends QuartzJobBean {
    private final Job job;
    private final JobLauncher jobLauncher;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(job, parameters);
        } catch (Exception e) {
            log.error("Job execution failed", e);
            throw new JobExecutionException(e);
        }
    }
}