package com.booksync.app.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "account")
public class Account {
    private static final long serialVersionUID = 2L;

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String name;

    public Account(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
