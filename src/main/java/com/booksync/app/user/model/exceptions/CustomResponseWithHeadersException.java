package com.booksync.app.user.model.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class CustomResponseWithHeadersException extends CustomResponseException {
    private HttpHeaders headers;

    public CustomResponseWithHeadersException(String message, HttpStatus statusCode, HttpHeaders headers) {
        super(message, statusCode);
        this.headers = headers;
    }
}
