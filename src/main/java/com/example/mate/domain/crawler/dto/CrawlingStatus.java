package com.example.mate.domain.crawler.dto;

import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.crawler.constant.CrawlingStatusType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CrawlingStatus {
    private LocalDateTime lastRunTime;
    private CrawlingStatusType status = CrawlingStatusType.RUNNING;
    private String message;
    private int processedCount;
    private ErrorCode errorCode;

    public CrawlingStatus() {
        this.lastRunTime = LocalDateTime.now();
        this.message = CrawlingStatusType.RUNNING.getMessage();
        this.processedCount = 0;
    }

    public synchronized void start() {
        this.lastRunTime = LocalDateTime.now();
        this.status = CrawlingStatusType.RUNNING;
        this.message = CrawlingStatusType.RUNNING.getMessage();
        this.processedCount = 0;
        this.errorCode = null;
    }

    public synchronized void complete(int count) {
        this.status = CrawlingStatusType.COMPLETED;
        this.message = CrawlingStatusType.COMPLETED.getMessage();
        this.processedCount = count;
        this.errorCode = null;
    }

    public synchronized void fail(ErrorCode errorCode) {
        this.status = CrawlingStatusType.FAILED;
        this.message = errorCode.getMessage();
        this.errorCode = errorCode;
    }

    public synchronized CrawlingStatusResponse toResponse() {
        return new CrawlingStatusResponse(
                lastRunTime,
                status.name(),
                message,
                processedCount,
                errorCode != null ? errorCode.getCode() : null
        );
    }
}
