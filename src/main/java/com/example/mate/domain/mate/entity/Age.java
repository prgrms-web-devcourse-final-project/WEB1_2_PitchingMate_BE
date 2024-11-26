package com.example.mate.domain.mate.entity;

import lombok.Getter;

@Getter
public enum Age {
    ALL("상관 없음"),
    TEENS("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    OVER_FIFTIES("50대이상");

    private final String value;

    Age(String value) {
        this.value = value;
    }
}
