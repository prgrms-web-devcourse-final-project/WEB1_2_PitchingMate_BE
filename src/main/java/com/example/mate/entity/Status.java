package com.example.mate.entity;

import lombok.Getter;

@Getter
public enum Status {
    OPEN("모집중"),
    CLOSED("모집완료"),
    COMPLETE("직관완료");

    private final String value;

    Status(String value) {
        this.value = value;
    }
}
