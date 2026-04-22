package com.medev.hrstream.file;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioConfigUnitTest {

    @Test
    void createsBucketWhenBucketDoesNotExist() throws Exception {
        MinioProperties properties = new MinioProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setRegion("us-east-1");
        properties.setAccessKey("admin");
        properties.setSecretKey("admin123");
        properties.setBucket("ats-resumes");

        S3Client s3Client = mock(S3Client.class);
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(NoSuchBucketException.builder().message("missing").build());

        MinioConfig minioConfig = new MinioConfig(properties);
        ApplicationRunner runner = minioConfig.ensureBucketExistsRunner(s3Client);

        runner.run(new DefaultApplicationArguments());

        verify(s3Client, times(1)).headBucket(any(HeadBucketRequest.class));
        verify(s3Client, times(1)).createBucket(any(CreateBucketRequest.class));
    }
}


