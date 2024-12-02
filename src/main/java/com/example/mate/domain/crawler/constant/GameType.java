package com.example.mate.domain.crawler.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameType {
    REGULAR("0,9,6", "정규시즌"),
    POST("3,4,5,7", "포스트시즌"),
    EXHIBITION("1", "시범경기");

    private final String value;  // KBO 페이지의 실제 value 값
    private final String description;
}