package com.medev.hrstream.common;

import com.medev.hrstream.jobapplication.submission.CvFileRejectedException;
import com.medev.hrstream.jobapplication.submission.DuplicateApplicationException;
import com.medev.hrstream.jobapplication.submission.InvalidApplicationTokenException;
import com.medev.hrstream.jobapplication.submission.JobClosedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildError(HttpStatus.BAD_REQUEST, errors.toString(), request, "VALIDATION");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        return buildError((HttpStatus) ex.getStatusCode(), ex.getReason(), request, null);
    }

    @ExceptionHandler(JobClosedException.class)
    public ResponseEntity<ApiError> handleJobClosed(JobClosedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request, "JOB_CLOSED");
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateApplicationException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request, "DUPLICATE_APPLICATION");
    }

    @ExceptionHandler(InvalidApplicationTokenException.class)
    public ResponseEntity<ApiError> handleInvalidToken(InvalidApplicationTokenException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, "INVALID_APPLICATION_TOKEN");
    }

    @ExceptionHandler(CvFileRejectedException.class)
    public ResponseEntity<ApiError> handleCvRejected(CvFileRejectedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "CV_FILE_REJECTED");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request, null);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request, String code) {
        ApiError error = new ApiError(status.value(), status.getReasonPhrase(), message, request.getRequestURI(), code);
        return ResponseEntity.status(status).body(error);
    }
}
