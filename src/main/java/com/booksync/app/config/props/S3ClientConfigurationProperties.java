package com.booksync.app.config.props;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@ConfigurationProperties("s3.config")
@Getter
@Setter
@EqualsAndHashCode
public class S3ClientConfigurationProperties {
    private Region region;
    private URI endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private int multipartMinPartSize = 5*1024*1024;
    private String bucket;
}
