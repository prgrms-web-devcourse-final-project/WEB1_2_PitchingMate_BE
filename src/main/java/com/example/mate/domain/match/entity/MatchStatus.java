package com.example.mate.domain.match.entity;

import lombok.Getter;

@Getter
public enum MatchStatus {
    SCHEDULED("예정"),
    COMPLETED("종료"),
    CANCELED("취소");

    private final String description;

    MatchStatus(String description) {
        this.description = description;
    }
}