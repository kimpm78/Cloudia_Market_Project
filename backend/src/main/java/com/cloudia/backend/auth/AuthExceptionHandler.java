package com.cloudia.backend.auth;

import com.cloudia.backend.common.model.ResponseModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class AuthExceptionHandler {

    /**
     * DisabledException（休眠/退会アカウント）の処理
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ResponseModel<Void>> handleDisabledException(DisabledException ex) {

        log.warn("認証失敗 DisabledException: {}", ex.getMessage());

        String errorMessage = ex.getMessage();

        ResponseModel<Void> responseModel = ResponseModel.<Void>builder()
                .result(false)
                .message(errorMessage)
                .build();

        return new ResponseEntity<>(responseModel, HttpStatus.UNAUTHORIZED);
    }

    /**
     * UsernameNotFoundException（IDなし）の処理
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseModel<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {

        log.warn("認証失敗 UsernameNotFoundException: {}", ex.getMessage());

        String genericMessage = "IDまたはパスワードをご確認ください。";

        ResponseModel<Void> responseModel = ResponseModel.<Void>builder()
                .result(false)
                .message(genericMessage)
                .build();

        return new ResponseEntity<>(responseModel, HttpStatus.UNAUTHORIZED);
    }

    /**
     * BadCredentialsException（パスワード不一致）の処理
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseModel<Void>> handleBadCredentialsException(BadCredentialsException ex) {

        log.warn("認証失敗 BadCredentialsException: {}", ex.getMessage());

        String genericMessage = "IDまたはパスワードをご確認ください。";

        ResponseModel<Void> responseModel = ResponseModel.<Void>builder()
                .result(false)
                .message(genericMessage)
                .build();

        return new ResponseEntity<>(responseModel, HttpStatus.UNAUTHORIZED);
    }
}