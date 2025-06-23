package com.tilguys.matilda.common.exception;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MatildaExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ProblemDetail handleServiceLogicException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException occurred: {}", e.getMessage());
        log.error("error occur : {}", e.getMessage());
        return createErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                errors.getFirst()
        );
        problemDetail.setTitle("ValidationException");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("validationErrors", errors);
        return problemDetail;
    }

    @ExceptionHandler({Exception.class})
    public ProblemDetail handleException(Exception e) {
        log.error("error occur : {}", e.getMessage());
        return createErrorResponse(e, HttpStatus.BAD_REQUEST, "예기치 못한 예외가 발생하였습니다. 잠시 뒤 요청해주세요.");
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
