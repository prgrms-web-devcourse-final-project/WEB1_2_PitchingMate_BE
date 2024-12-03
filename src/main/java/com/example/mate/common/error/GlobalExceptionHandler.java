package com.example.mate.common.error;

import com.example.mate.common.response.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    // MethodArgumentNotValidException 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        // 유효성 검증 실패한 필드와 메시지 추출
        List<String> validationErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        String errorMessage = String.join(", ", validationErrors);

        // log 처리
        String formattedErrors = validationErrors.stream()
                .map(error -> " - " + error)
                .collect(Collectors.joining("\n"));
        log.error("Validation failed for the following fields:\n{}", formattedErrors);

        // ApiResponse 반환
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        errorMessage,
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        e.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    // NumberFormatException 처리
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleNumberFormatException(NumberFormatException e) {
        log.error("NumberFormatException: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        "잘못된 요청 형식입니다.",
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());

        String errorMessage = String.format("잘못된 입력 형식입니다. '%s' 타입이 '%s'로 변환될 수 없습니다.",
                e.getValue(), e.getRequiredType().getSimpleName());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        errorMessage,
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    // MissingServletRequestParameterException 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage());

        // 누락된 파라미터를 메시지로 반환
        String errorMessage = String.format("요청에 '%s' 파라미터가 누락되었습니다.", e.getParameterName());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        errorMessage,
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