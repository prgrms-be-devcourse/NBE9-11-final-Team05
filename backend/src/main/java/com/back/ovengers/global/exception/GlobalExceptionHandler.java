package com.back.ovengers.global.exception;

import com.back.ovengers.global.response.ApiResponse;
import jakarta.persistence.LockTimeoutException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(
            CustomException e
    ) {

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        new ApiResponse<>(
                                errorCode.name(),
                                errorCode.getMessage()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException e
    ) {

        FieldError fieldError =
                e.getBindingResult().getFieldErrors().get(0);

        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiResponse<>(
                                ErrorCode.MISSING_REQUIRED_FIELD.name(),
                                field + ": " + message
                        )
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        return ResponseEntity
                .badRequest()
                .body(
                        new ApiResponse<>(
                                ErrorCode.INVALID_PATH_VARIABLE.name(),
                                message
                        )
                );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(
            Exception e
    ) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ApiResponse<>(
                                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                        )
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(
            AccessDeniedException e
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ApiResponse<>(
                                ErrorCode.FORBIDDEN.name(),
                                ErrorCode.FORBIDDEN.getMessage()
                        )
                );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse> handleOptimisticLockException(
            ObjectOptimisticLockingFailureException e
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiResponse<>(
                                ErrorCode.OPTIMISTIC_LOCK_CONFLICT.name(),
                                ErrorCode.OPTIMISTIC_LOCK_CONFLICT.getMessage()
                        )
                );
    }

    @ExceptionHandler(LockTimeoutException.class)
    public ResponseEntity<ApiResponse> handleLockTimeoutException(
            LockTimeoutException e
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        new ApiResponse<>(
                                ErrorCode.LOCK_TIMEOUT.name(),
                                ErrorCode.LOCK_TIMEOUT.getMessage()
                        )
                );
    }
}