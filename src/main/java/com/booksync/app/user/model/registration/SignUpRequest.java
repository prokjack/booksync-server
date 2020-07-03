package com.booksync.app.user.model.registration;

import com.booksync.app.user.model.User;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SignUpRequest {
    @NotNull
    private User user;

}
