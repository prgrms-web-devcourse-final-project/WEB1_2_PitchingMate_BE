package com.example.mate.domain.constant;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Rating {
    BAD("별로예요"),
    GOOD("좋아요!"),
    GREAT("최고예요!");

    @JsonValue
    private final String value;

    Rating(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Rating from(String value) {
        for (Rating rate : Rating.values()) {
            if (rate.value.equals(value))
                return rate;
        }
        throw new CustomException(ErrorCode.INVALID_AGE_VALUE);
    }
}