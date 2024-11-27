package com.example.mate.common.error;

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
    STADIUM_NOT_FOUND_BY_NAME(HttpStatus.NOT_FOUND, "S002", "해당 이름의 경기장 정보를 찾을 수 없습니다"),

    // Member
    MEMBER_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "M001", "해당 ID의 회원 정보를 찾을 수 없습니다"),

    // FILE
    FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "F001", "빈 파일을 업로드할 수 없습니다. 파일 내용을 확인해주세요."),
    FILE_UNSUPPORTED_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "F002", "지원하지 않는 파일 형식입니다."),
    FILE_MISSING_MIME_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "F003", "파일의 MIME 타입을 찾을 수 없습니다."),
    FILE_UPLOAD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "F004", "이미지 파일은 최대 10개까지 업로드 할 수 있습니다."),

    // Goods
    GOODS_IMAGES_ARE_EMPTY(HttpStatus.BAD_REQUEST, "G001", "굿즈 이미지는 최소 1개 이상을 업로드 할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
