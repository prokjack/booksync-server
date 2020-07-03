package com.booksync.app.user.model.registration;

import com.booksync.app.user.model.User;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SignUpRequest {
    @NotEmpty
    private String email;
    @NotNull
    private User user;

}
