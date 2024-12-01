package com.example.mate.domain.crawler.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CrawlingStatusResponse {
    private LocalDateTime lastRunTime;
    private String status;
    private String message;
    private int processedCount;
    private String errorCode;
}
