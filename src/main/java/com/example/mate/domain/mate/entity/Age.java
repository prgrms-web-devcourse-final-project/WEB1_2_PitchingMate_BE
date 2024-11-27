package com.example.mate.domain.mate.entity;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Age {
    ALL("상관 없음"),
    TEENS("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    OVER_FIFTIES("50대이상");

    @JsonValue
    private final String value;

    Age(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Age from(String value) {
        for (Age age : Age.values()) {
            if (age.value.equals(value))
                return age;
        }
        throw new CustomException(ErrorCode.INVALID_AGE_VALUE);
    }
}
