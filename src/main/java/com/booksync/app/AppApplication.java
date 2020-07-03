package com.booksync.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableWebFlux
@EnableReactiveMongoRepositories
@ComponentScan({"com.booksync.app.config", "com.booksync.app.user"})
public class AppApplication {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
//        BlockHound.install();
        SpringApplication.run(AppApplication.class, args);
    }

}
