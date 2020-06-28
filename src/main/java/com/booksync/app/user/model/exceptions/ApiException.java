package com.booksync.app.user.model.exceptions;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
    public abstract HttpStatus getStatusCode();
    public abstract String getMessage();
}
