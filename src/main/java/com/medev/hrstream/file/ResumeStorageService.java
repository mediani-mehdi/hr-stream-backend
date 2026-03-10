package com.medev.hrstream.file;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Resume storage service using MinIO.
 *
 * This service uploads resumes to a MinIO bucket and provides presigned URLs for secure access.
 */
@Service
public class ResumeStorageService {

    public record StoredObject(
            String bucket,
            String objectKey,
            String url,
            String originalName,
            String contentType,
            long sizeBytes
    ) {}

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public ResumeStorageService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public StoredObject uploadCandidateResume(String candidateId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        String bucket = properties.getBucket();

        String original = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > -1 && idx < original.length() - 1) {
            ext = original.substring(idx);
        }

        String objectKey = String.format(
                "resumes/%s/%s%s",
                candidateId,
                UUID.randomUUID(),
                ext
        );

        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .contentType(file.getContentType())
                            .stream(in, file.getSize(), -1)
                            .build()
            );

            // Generate a presigned URL that is valid for 1 hour for immediate use if needed
            String presignedUrl = getResumeViewUrl(objectKey);

            return new StoredObject(
                    bucket,
                    objectKey,
                    presignedUrl,
                    original,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload resume to MinIO", e);
        }
    }

    public String getResumeViewUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for resume", e);
        }
    }
}
