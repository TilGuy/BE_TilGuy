package com.tilguys.matilda.til.exceptions;

import com.tilguys.matilda.til.controller.TilController;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {TilController.class})
public class TilExceptionHandler {
    
    @ExceptionHandler({IllegalArgumentException.class})
    public ProblemDetail handleServiceLogicException(IllegalArgumentException e) {
        log.error("IllegalArgumentException occurred: {}", e.getMessage(), e);
        return createErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    private ProblemDetail createErrorResponse(Exception e, HttpStatus status) {
        return createErrorResponse(e, status, e.getMessage());
    }

    private ProblemDetail createErrorResponse(Exception e, HttpStatus status, String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setTitle(e.getClass().getSimpleName());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }
}
