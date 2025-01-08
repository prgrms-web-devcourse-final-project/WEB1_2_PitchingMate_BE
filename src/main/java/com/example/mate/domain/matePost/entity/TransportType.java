package com.example.mate.domain.matePost.entity;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TransportType {
    ANY("상관없음"),
    PUBLIC("대중교통"),
    CAR("자차");
    @JsonValue
    private final String value;

    TransportType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TransportType from(String value) {
        for (TransportType type : TransportType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new CustomException(ErrorCode.INVALID_TRANSPORT_TYPE_VALUE);
    }
}
