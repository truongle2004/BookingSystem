package com.bookingsystem.platform.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/** Maps MVC failures to stable, non-sensitive API errors. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        MissingRequestValueException.class,
        TypeMismatchException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<ApiErrorResponse> invalidRequest(final HttpServletRequest request) {
        return response(request, HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "The request is invalid.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> malformedRequest(final HttpServletRequest request) {
        return response(request, HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The request body is malformed.");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(final HttpServletRequest request) {
        return response(request, HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> methodNotAllowed(final HttpServletRequest request) {
        return response(request, HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "The method is not allowed.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> internalError(
            final HttpServletRequest request,
            final Exception exception) {
        LOGGER.error("Unhandled request failure", exception);
        return response(request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.");
    }

    private ResponseEntity<ApiErrorResponse> response(
            final HttpServletRequest request,
            final HttpStatus status,
            final String code,
            final String message) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(code, message, RequestIdFilter.currentRequestId(request)));
    }
}
