package com.medev.hrstream.file;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO/S3 configuration class.
 * Creates the S3Client bean and ensures the target bucket exists on startup.
 */
@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    private final MinioProperties properties;

    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }

    /**
     * Provides a S3Client bean configured with the given MinIO properties.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(java.net.URI.create(properties.getEndpoint()))
                .region(software.amazon.awssdk.regions.Region.of(properties.getRegion()))
                .credentialsProvider(software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                        software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(
                                properties.getAccessKey(), properties.getSecretKey()
                        )
                ))
                .build();
    }

    /**
     * Ensures the configured bucket exists on application startup.
     * Creates it if it does not exist.
     */
    @PostConstruct
    public void ensureBucketExists() {
        try {
            S3Client client = s3Client();
            String bucket = properties.getBucket();
            try {
                client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
                log.info("Bucket '{}' already exists.", bucket);
            } catch (NoSuchBucketException e) {
                log.info("Bucket '{}' does not exist. Creating it...", bucket);
                client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("Bucket '{}' created successfully.", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to ensure S3 bucket exists: {}", e.getMessage(), e);
            throw new RuntimeException("S3 bucket initialization failed", e);
        }
    }
}
