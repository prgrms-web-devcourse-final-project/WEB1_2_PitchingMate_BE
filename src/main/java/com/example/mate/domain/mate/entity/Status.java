package com.example.mate.domain.mate.entity;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Status {
    OPEN("모집중"),
    CLOSED("모집완료"),
    VISIT_COMPLETE("직관완료");

    @JsonValue
    private final String value;

    Status(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Status from(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new CustomException(ErrorCode.INVALID_STATUS_TYPE_VALUE);
    }
}
