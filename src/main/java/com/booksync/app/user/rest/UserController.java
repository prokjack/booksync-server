package com.booksync.app.user.rest;

import com.booksync.app.config.security.ReactiveUserDetailsServiceImpl;
import com.booksync.app.user.model.ApiResponse;
import com.booksync.app.user.model.Authority;
import com.booksync.app.user.model.User;
import com.booksync.app.user.model.exceptions.UserAlreadyExistException;
import com.booksync.app.user.model.registration.SignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Set;

import static com.booksync.app.config.security.AuthoritiesConstants.USER;
import static com.booksync.app.user.model.ApiResponse.Status.SUCCESS;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final PasswordEncoder bCryptPasswordEncoder;
    private final ReactiveUserDetailsServiceImpl userDetailsService;

    @GetMapping("current")
    private Mono<Principal> getCurrentUser(Principal user) {
        return Mono.just(user);
    }

    @GetMapping("/")
    public Mono<String> index(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
        return oauth2User
                .map(OAuth2User::getName)
                .map(name -> String.format("Hi, %s", name));
    }

    @PostMapping("/sign-up")
    public Mono<ApiResponse<User>> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
        User user = signUpRequest.getUser();
        user.setAuthorities(Set.of(new Authority(USER)));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userDetailsService.getByUsername(user.getLogin())
                .switchIfEmpty(userDetailsService.saveNewUser(user))
                .flatMap(userDB -> {
                    if (userDB != null) {
                        return Mono.error(new UserAlreadyExistException(user));
                    }
                    return userDetailsService.saveNewUser(user).map(u -> new ApiResponse<>(SUCCESS, u));
                });
    }
}