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
@Document(collection = "s3_uploads")
public class S3Upload {

    @Id
    private String id;

    @Indexed(unique = true)
    private String key;

    private String filename;

    private String userName;

    public S3Upload(String key, String filename, String userName) {
        this.key = key;
        this.filename = filename;
        this.userName = userName;
    }
}
