package com.example.mate.domain.crawler.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrawlingStatusType {
    RUNNING("크롤링이 진행 중입니다"),
    COMPLETED("크롤링이 완료되었습니다"),
    FAILED("크롤링이 실패했습니다");

    private final String message;
}