package com.booksync.app.user.service;

import com.booksync.app.user.model.Account;
import com.booksync.app.user.model.registration.SignUpRequest;
import com.booksync.app.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccountsService {
    private final AccountRepository accountRepository;

    public Mono<Account> createAccount(SignUpRequest signUpRequest) {
        Account account = new Account(signUpRequest.getEmail(), signUpRequest.getUser().getLogin());
        return accountRepository.save(account);
    }
}
