package com.medev.hrstream.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("hr_stream_test")
            .withUsername("testuser")
            .withPassword("testpass");

    protected static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    protected static final GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "admin")
            .withEnv("MINIO_ROOT_PASSWORD", "admin123")
            .withCommand("server /data")
            .withStartupTimeout(Duration.ofSeconds(30));

    // Static block starts containers once for the entire JVM run.
    // Testcontainers' Ryuk sidecar handles cleanup on JVM exit.
    static {
        postgres.start();
        redis.start();
        minio.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("minio.endpoint", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.access-key", () -> "admin");
        registry.add("minio.secret-key", () -> "admin123");
        registry.add("minio.bucket", () -> "ats-resumes-test");
        registry.add("minio.region", () -> "us-east-1");
    }

    @BeforeAll
    static void setUp() {
        createBucket();
    }

    private static void createBucket() {
        try {
            var client = software.amazon.awssdk.services.s3.S3Client.builder()
                    .endpointOverride(java.net.URI.create(
                            "http://" + minio.getHost() + ":" + minio.getMappedPort(9000)))
                    .region(software.amazon.awssdk.regions.Region.US_EAST_1)
                    .credentialsProvider(software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                            software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create("admin", "admin123")))
                    .forcePathStyle(true)
                    .build();
            try {
                client.createBucket(b -> b.bucket("ats-resumes-test"));
            } catch (software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException |
                     software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException ignored) {
            }
            client.close();
        } catch (Exception e) {
            throw new IllegalStateException("could not create MinIO test bucket", e);
        }
    }
}
