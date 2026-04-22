package com.medev.hrstream.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;

    @PostConstruct
    public void validate() {
        Assert.hasText(endpoint, "minio.endpoint must be set");
        Assert.hasText(accessKey, "minio.accessKey must be set");
        Assert.hasText(secretKey, "minio.secretKey must be set");
        Assert.hasText(bucket, "minio.bucket must be set");
        Assert.hasText(region, "minio.region must be set");
    }
}
