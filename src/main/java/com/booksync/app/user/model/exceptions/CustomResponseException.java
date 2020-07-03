package com.booksync.app.user.model.exceptions;

import com.booksync.app.user.model.ApiResponse;
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
    private ApiResponse<Object> body;

    CustomResponseException(ApiResponse<Object> body) {
        this.body = body;
    }

    CustomResponseException(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        if (statusCode == null) {
            if (body != null) {
                return body.getStatusCode();
            }
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return statusCode;
    }
}
