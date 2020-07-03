package com.booksync.app.user.repository;

import com.booksync.app.user.model.S3Upload;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface S3UploadsRepository extends ReactiveMongoRepository<S3Upload, String> {
    Flux<S3Upload> findAllByUserName(String userName);
}