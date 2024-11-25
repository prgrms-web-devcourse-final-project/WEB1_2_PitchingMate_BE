package com.example.mate.entity;

import lombok.Getter;

@Getter
public enum Rating {
    BAD("별로예요"),
    GOOD("좋아요!"),
    GREAT("최고예요!");

    private final String value;

    Rating(String value) {
        this.value = value;
    }
}
