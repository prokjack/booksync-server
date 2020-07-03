package com.booksync.app.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@EqualsAndHashCode

public class ApiResponse<T> {

    private Status status;
    private T body;
    private HttpStatus statusCode;
    private String errorMessage;

    public ApiResponse(Status status, T body, HttpStatus statusCode, String errorMessage) {
        this.status = status;
        this.body = body;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public ApiResponse(Status status, T body) {
        this.status = status;
        this.body = body;
    }

    public ApiResponse(Status status, T body, HttpStatus statusCode) {
        this.status = status;
        this.body = body;
        this.statusCode = statusCode;
    }

    public enum Status {SUCCESS,FAILED}
}
