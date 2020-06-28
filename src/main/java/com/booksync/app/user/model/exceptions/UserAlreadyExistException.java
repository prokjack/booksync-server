package com.booksync.app.user.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserAlreadyExistException extends CustomResponseException {
    public UserAlreadyExistException(HttpStatus statusCode, String message) {
        super(message, statusCode);
    }
}
