package com.cloudia.backend.common.exception;

import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * BaseException 処理
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();

        LogHelper.log(
                LogMessage.COMMON_BUSINESS_EXCEPTION,
                new String[] { errorCode.getCode(), errorCode.getMessage() });

        return new ResponseEntity<>(ErrorResponse.of(errorCode), errorCode.getStatus());
    }

    /**
     * DataAccessException 処理
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        LogHelper.log(
                LogMessage.DB_ACCESS_ERROR,
                new String[] { "Database access error" },
                e);

        return new ResponseEntity<>(
                ErrorResponse.of(ErrorCode.DATABASE_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validation 例外処理 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String firstError = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .orElse("validation error");

        LogHelper.log(
                LogMessage.VALIDATION_INPUT_INVALID,
                new String[] { firstError });

        return buildValidationErrorResponse(e.getBindingResult().getFieldErrors());
    }

    /**
     * BindException 処理
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        LogHelper.log(
                LogMessage.VALIDATION_INPUT_INVALID,
                new String[] { "Binding error" });

        return buildValidationErrorResponse(e.getBindingResult().getFieldErrors());
    }

    /**
     * Type Mismatch 例外処理
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {

        LogHelper.log(LogMessage.VALIDATION_TYPE_MISMATCH,
                new String[] {
                        e.getName(),
                        String.valueOf(e.getValue())
                });

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * HTTP Method 例外処理
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {

        LogHelper.log(
                LogMessage.HTTP_METHOD_NOT_SUPPORTED,
                new String[] { e.getMethod() });

        return new ResponseEntity<>(
                ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Exception 処理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        LogHelper.log(
                LogMessage.COMMON_UNEXPECTED_ERROR,
                new String[] { "Unexpected server error" },
                e);

        return new ResponseEntity<>(
                ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validation 共通処理
     */
    private ResponseEntity<ErrorResponse> buildValidationErrorResponse(
            Iterable<FieldError> fieldErrors) {

        Map<String, String> errors = new HashMap<>();
        fieldErrors.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return new ResponseEntity<>(
                ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors),
                HttpStatus.BAD_REQUEST);
    }
}
