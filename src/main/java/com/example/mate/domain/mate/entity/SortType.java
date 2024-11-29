package com.example.mate.domain.mate.entity;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SortType {
    LATEST("최근 작성일 순"),
    MATCH_TIME("가까운 경기 순"),
    MANNER("매너타율 순");

    @JsonValue
    private final String value;

    SortType(String value) {this.value = value;}

    @JsonCreator
    public static SortType from(String value) {
        for (SortType type : SortType.values()) {
            if (type.value.equals(value))
                return type;
        }
        throw new CustomException(ErrorCode.INVALID_SORT_TYPE_VALUE);
    }
}
