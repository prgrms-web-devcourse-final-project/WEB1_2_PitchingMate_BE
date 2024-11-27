package com.example.mate.common.error;

import com.example.mate.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }

    // 유효성 검사 오류가 발생한 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation failed: {}", e.getMessage(), e);
        
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        "유효성 검사 오류가 발생했습니다.",
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    // 모든 예외 타입 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(
                        "서버 내부 오류가 발생했습니다.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }
}