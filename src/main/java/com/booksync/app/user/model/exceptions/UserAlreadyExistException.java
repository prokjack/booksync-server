package com.booksync.app.user.model.exceptions;

import com.booksync.app.user.model.ApiResponse;
import com.booksync.app.user.model.User;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserAlreadyExistException extends CustomResponseException {

    public UserAlreadyExistException(User user) {
        super(new ApiResponse<>(ApiResponse.Status.FAILED, null, HttpStatus.CONFLICT, "User " + user.getLogin() +" already exist"));
    }
}
