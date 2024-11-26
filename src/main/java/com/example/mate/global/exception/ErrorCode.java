package com.example.mate.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "Internal Server Error"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "Invalid Input Value"),

    // Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "팀을 찾을 수 없습니다"),

    // Stadium
    STADIUM_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "S001", "해당 ID의 경기장 정보를 찾을 수 없습니다"),
    STADIUM_NOT_FOUND_BY_NAME(HttpStatus.NOT_FOUND, "S002", "해당 이름의 경기장 정보를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
