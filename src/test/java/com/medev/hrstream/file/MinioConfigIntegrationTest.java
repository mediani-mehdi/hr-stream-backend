package com.medev.hrstream.file;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = MinioConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "minio.endpoint=http://localhost:9000",
                "minio.access-key=admin",
                "minio.secret-key=admin123",
                "minio.bucket=ats-resumes",
                "minio.region=us-east-1"
        }
)
class MinioConfigIntegrationTest {

    @Autowired
    private ApplicationRunner ensureBucketExistsRunner;

    @MockBean
    private S3Client s3Client;

    @Test
    void contextLoadsAndRunsBucketCheckOnStartup() {
        assertThat(ensureBucketExistsRunner).isNotNull();
        verify(s3Client, atLeastOnce()).headBucket(any());
    }
}

