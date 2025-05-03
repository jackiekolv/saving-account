package com.example.savingAccount.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private record ErrorResponse(
            String message,
            int status,
            String errorCode,
            LocalDateTime timestamp,
            Object details,
            String path
    ) {}

    private ResponseEntity<ErrorResponse> buildError(
            String message,
            HttpStatus status,
            String errorCode,
            Object details,
            String path
    ) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        message,
                        status.value(),
                        errorCode,
                        LocalDateTime.now(),
                        details,
                        path
                )
        );
    }

    @ExceptionHandler(BaseHttpException.class)
    public ResponseEntity<?> handleBaseHttpException(BaseHttpException ex, WebRequest request) {
        log.warn("Handled custom error: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        return buildError(
                ex.getMessage(),
                ex.getStatus(),
                ex.getClass().getSimpleName(),
                null,
                path
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildError(
                "Access denied",
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
                .toList();

        return buildError(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                errors,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<?> handleMissingRequest(MissingRequestValueException ex, HttpServletRequest request) {
        log.warn("Missing request value: {}", ex.getMessage());
        return buildError(
                "Missing required parameter",
                HttpStatus.BAD_REQUEST,
                "MISSING_PARAM",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildError(
                "Unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );
    }
}
