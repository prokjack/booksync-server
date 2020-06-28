package com.booksync.app.user.repository;

import com.booksync.app.user.model.Authority;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends ReactiveMongoRepository<Authority, String> {
}