package com.medev.hrstream.file;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Resume/CV storage service using S3 (compatible with MinIO).
 *
 * Handles upload, presigned URL generation, and deletion of CV files.
 */
@Service
public class ResumeStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx"
    );

    public record StoredObject(
            String bucket,
            String objectKey,
            String url,
            String originalName,
            String contentType,
            long sizeBytes
    ) {}

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final MinioProperties properties;

    public ResumeStorageService(S3Client s3Client, MinioProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.s3Presigner = S3Presigner.builder()
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
     * Validates and uploads a candidate's CV to S3.
     *
     * @param candidateId the candidate ID
     * @param file        the multipart file to upload
     * @return metadata about the stored object
     */
    public StoredObject uploadCandidateCv(String candidateId, MultipartFile file) {
        validateFile(file);

        String bucket = properties.getBucket();
        String original = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String ext = extractExtension(original);

        String objectKey = String.format(
                "cvs/%s/%s.%s",
                candidateId,
                UUID.randomUUID(),
                ext
        );

        try (InputStream in = file.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(in, file.getSize())
            );

            String presignedUrl = getCvViewUrl(objectKey);

            return new StoredObject(
                    bucket,
                    objectKey,
                    presignedUrl,
                    original,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload CV to S3", e);
        }
    }

    /**
     * Generates a presigned GET URL valid for 1 hour.
     */
    public String getCvViewUrl(String objectKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for CV", e);
        }
    }

    /**
     * Deletes a CV object from S3.
     */
    public void deleteCv(String objectKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete CV from S3", e);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CV file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("CV file exceeds the maximum allowed size of 10 MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only PDF and Word documents (pdf, doc, docx) are allowed"
            );
        }

        // Also validate extension as a secondary check
        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String ext = extractExtension(originalName).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new IllegalArgumentException(
                        "Invalid file extension. Only .pdf, .doc, and .docx are allowed"
                );
            }
        }
    }

    private String extractExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx > -1 && idx < filename.length() - 1) {
            return filename.substring(idx + 1);
        }
        return "bin";
    }
}
