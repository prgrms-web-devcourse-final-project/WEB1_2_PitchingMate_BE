package com.example.mate.domain.constant;

import lombok.Getter;

@Getter
public enum Gender {
    ANY("상관없음"),
    MALE("남자만"),
    FEMALE("여자만");

    private final String value;

    Gender(String value) {
        this.value = value;
    }
}
