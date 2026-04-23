package com.example.communicationoptimizer.controller;

import com.example.communicationoptimizer.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(NoSuchElementException exception) {
        return new ApiResponse<>(404, exception.getMessage(), null, "demo-request-id");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationError(MethodArgumentNotValidException exception) {
        return new ApiResponse<>(400, "invalid request", null, "demo-request-id");
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleUnsupportedProvider(UnsupportedOperationException exception) {
        return new ApiResponse<>(400, exception.getMessage(), null, "demo-request-id");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalState(IllegalStateException exception) {
        exception.printStackTrace();
        return new ApiResponse<>(400, exception.getMessage(), null, "demo-request-id");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpected(Exception exception) {
        logger.error("Unhandled exception", exception);
        return new ApiResponse<>(500, exception.getClass().getSimpleName() + ": " + exception.getMessage(), null, "demo-request-id");
    }
}
