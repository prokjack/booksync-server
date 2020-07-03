package com.booksync.app.config.security;

import com.booksync.app.user.model.exceptions.CustomResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WebFluxExceptionHandler {

    @ExceptionHandler(CustomResponseException.class)
    public ResponseEntity handlerWebClientException(CustomResponseException customResponseException) {
        return ResponseEntity.status(customResponseException.getStatusCode()).body(customResponseException.getBody());
    }
}