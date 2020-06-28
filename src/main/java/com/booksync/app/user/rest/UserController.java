package com.booksync.app.user.rest;

import com.booksync.app.config.security.ReactiveUserDetailsServiceImpl;
import com.booksync.app.user.model.Authority;
import com.booksync.app.user.model.User;
import com.booksync.app.user.model.exceptions.ApiException;
import com.booksync.app.user.model.exceptions.CustomResponseException;
import com.booksync.app.user.model.exceptions.UserAlreadyExistException;
import com.mongodb.internal.connection.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

import static com.booksync.app.config.security.AuthoritiesConstants.USER;

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
    public Mono<ResponseEntity<User>> signUp(@RequestBody User user) {
        user.setAuthorities(Set.of(new Authority(USER)));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userDetailsService.getByUsername(user.getLogin())
                .switchIfEmpty(userDetailsService.saveNewUser(user))
                .flatMap(userDB -> {
                    if (userDB != null) {
                        throw new UserAlreadyExistException(HttpStatus.CONFLICT, "User is already exist");
                    }
                    return userDetailsService.saveNewUser(user);
                })
                .map(ResponseEntity::ok);
//                .onErrorResume(e -> {
//                    if (e instanceof UserAlreadyExistException) {
//                        return ResponseEntity.status(HttpStatus.CONFLICT).body("")
//                    }
//                });
//        return mono
//                .onErrorResume(e -> {
//                    if (e instanceof ApiException) {
//                        ApiException ex = (ApiException) e;
//                        return ResponseEntity.status(ex.getStatusCode()).build(Mono.just(ex.getMessage()));
//                    }
//                    return ResponseEntity.status(500).build(Mono.just("Unknown Exception"));
//                });
    }

    @ExceptionHandler(CustomResponseException.class)
    public ResponseEntity handleException(CustomResponseException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(e.getMessage());
    }
}