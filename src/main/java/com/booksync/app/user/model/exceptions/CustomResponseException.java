package com.booksync.app.user.model.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomResponseException extends ApiException {
    private String message;
    private HttpStatus statusCode;
}
