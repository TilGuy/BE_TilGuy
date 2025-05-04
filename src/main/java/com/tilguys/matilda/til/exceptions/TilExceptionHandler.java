package com.tilguys.matilda.til.exceptions;

import com.tilguys.matilda.til.controller.TilController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {TilController.class})
public class TilExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleServiceLogicException(IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
