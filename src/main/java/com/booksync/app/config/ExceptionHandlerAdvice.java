package com.booksync.app.config;

import com.booksync.app.user.model.exceptions.CustomResponseException;
import com.booksync.app.user.model.exceptions.CustomResponseWithHeadersException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {
    private final List<String> allowedProxyHeaders = Arrays.asList("content-type");

    @ExceptionHandler(CustomResponseException.class)
    public ResponseEntity handleException(CustomResponseException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(e.getMessage());
    }

    @ExceptionHandler(CustomResponseWithHeadersException.class)
    public ResponseEntity handleException(CustomResponseWithHeadersException e) {
        HttpHeaders headers = e.getHeaders();
        HttpHeaders filteredHeaders = new HttpHeaders();
        headers.keySet().stream()
                .filter(headerName -> allowedProxyHeaders.contains(headerName.toLowerCase()))
                .forEach(headerName -> filteredHeaders.put(headerName, headers.get(headerName)));

        return ResponseEntity
                .status(e.getStatusCode())
                .headers(filteredHeaders)
                .body(e.getMessage());
    }
}