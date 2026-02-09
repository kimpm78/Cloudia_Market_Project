package com.cloudia.backend.common.exception;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String code;
    private String message;
    private Map<String, String> errors;
    private List<FieldErrorDetail> fieldErrors;

    private ErrorResponse(ErrorCode errorCode) {
        this.timestamp = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    private ErrorResponse(ErrorCode errorCode, Map<String, String> errors) {
        this.timestamp = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }

    private ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
        this.timestamp = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.fieldErrors = FieldErrorDetail.of(fieldErrors);
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(ErrorCode errorCode, Map<String, String> errors) {
        return new ErrorResponse(errorCode, errors);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode, fieldErrors);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldErrorDetail {
        private String field;
        private String value;
        private String reason;

        private FieldErrorDetail(FieldError fieldError) {
            this.field = fieldError.getField();
            this.value = fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString();
            this.reason = fieldError.getDefaultMessage();
        }

        public static List<FieldErrorDetail> of(List<FieldError> fieldErrors) {
            List<FieldErrorDetail> fieldErrorDetails = new ArrayList<>();
            fieldErrors.forEach(error -> fieldErrorDetails.add(new FieldErrorDetail(error)));
            return fieldErrorDetails;
        }
    }
}
