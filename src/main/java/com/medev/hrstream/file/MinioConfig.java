package com.medev.hrstream.file;

import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import org.springframework.boot.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

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
        try {
            return S3Client.builder()
                    .endpointOverride(URI.create(properties.getEndpoint()))
                    .region(software.amazon.awssdk.regions.Region.of(properties.getRegion()))
                    .forcePathStyle(true)
                    .credentialsProvider(software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                            software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(
                                    properties.getAccessKey(), properties.getSecretKey()
                            )
                    ))
                    .build();
        } catch (NullPointerException e) {
            log.error("Failed to create S3Client. Please check your MinIO configuration. " +
                    "Ensure that MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, MINIO_BUCKET, and MINIO_REGION are set.", e);
            return null; // Return null to prevent application crash
        }
    }

    /**
     * Ensures the configured bucket exists once the application context is ready.
     */
    @Bean
    public ApplicationRunner ensureBucketExistsRunner(S3Client s3Client) {
        return args -> {
            if (s3Client == null) {
                log.warn("S3Client is not available. Skipping bucket creation. Please check your MinIO configuration.");
                return;
            }
            try {
                String bucket = properties.getBucket();
                try {
                    s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
                    log.info("Bucket '{}' already exists.", bucket);
                } catch (NoSuchBucketException e) {
                    log.info("Bucket '{}' does not exist. Creating it...", bucket);
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                    log.info("Bucket '{}' created successfully.", bucket);
                }
            } catch (Exception e) {
                log.error("Failed to ensure S3 bucket exists: {}", e.getMessage(), e);
                throw new RuntimeException("S3 bucket initialization failed", e);
            }
        };
    }
}
