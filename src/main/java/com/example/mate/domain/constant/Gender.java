package com.example.mate.domain.constant;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Gender {
    ANY("상관없음"),
    MALE("남자"),
    FEMALE("여자");

    @JsonValue
    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Gender from(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.value.equals(value)) {
                return gender;
            }
        }
        throw new CustomException(ErrorCode.INVALID_GENDER_VALUE);
    }

    // M 또는 F를 기반으로 Gender를 반환하는 메서드 추가
    public static Gender fromCode(String code) {
        return switch (code.toUpperCase()) {
            case "M" -> MALE;
            case "F" -> FEMALE;
            default -> throw new CustomException(ErrorCode.INVALID_GENDER_VALUE);
        };
    }
}
