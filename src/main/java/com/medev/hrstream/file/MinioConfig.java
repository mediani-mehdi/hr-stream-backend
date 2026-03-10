package com.medev.hrstream.file;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO configuration class.
 */
@Configuration
public class MinioConfig {

    /**
     * Provides a MinioClient bean configured with the given MinIO properties.
     *
     * @param properties the MinIO properties
     * @return the MinioClient bean
     */
    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
