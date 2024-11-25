package com.example.mate.global.common.response;

import com.example.mate.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {
    private final String status;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;
    private final int code;

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .data(data)
                .timestamp(LocalDateTime.now())
                .code(200)
                .build();
    }

    // 에러 응답 - ErrorCode
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .code(errorCode.getStatus().value())
                .build();
    }

    // 에러 응답
    public static <T> ApiResponse<T> error(String message, int code) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .code(code)
                .build();
    }
}