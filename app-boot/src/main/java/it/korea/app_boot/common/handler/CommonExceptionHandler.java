package it.korea.app_boot.common.handler;

import java.nio.file.AccessDeniedException;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import it.korea.app_boot.common.dto.ErrorResponse;

@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * Exception 핸들러
     * @param e Exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        if (isSecurityException(e)) {
            throw (RuntimeException) e;  // security 예외는 다시 던짐
        }

        String message = e.getMessage() != null && e.getMessage().length() > 0 ? e.getMessage() : "서버에 오류가 있습니다.";
        ErrorResponse err = new ErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    /**
     * RuntimeException 핸들러
     * @param e RuntimeException
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(Exception e) {

        if (isSecurityException(e)) {
            throw (RuntimeException) e;  // security 예외는 다시 던짐
        }

        String message = e.getMessage() != null && e.getMessage().length() > 0 ? e.getMessage() : "서버에 오류가 있습니다.";
        ErrorResponse err = new ErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    private boolean isSecurityException(Exception e) {
        return e instanceof AuthenticationException ||
            e instanceof AccessDeniedException ||
            e instanceof AuthenticationCredentialsNotFoundException;
    }
}
