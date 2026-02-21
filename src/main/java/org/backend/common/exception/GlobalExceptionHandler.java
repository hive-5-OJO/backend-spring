package org.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.backend.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<CommonResponse<?>> handleDuplicateException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.error("CustomException: {}", errorCode.getMessage());
        return new ResponseEntity<>(CommonResponse.error(errorCode.getMessage()), errorCode.getStatus());

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CommonResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("요청값이 올바르지 않습니다.");
        log.warn("Validation Failed: {}", message);
        return new ResponseEntity<>(CommonResponse.error(message), HttpStatus.BAD_REQUEST);

    }

    // 그 외 예상치 못한 에러
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<CommonResponse<?>> handleException(Exception e){
        log.error("Unhandled Exception: "+e);
        return new ResponseEntity<>(CommonResponse.error("internal sever error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}