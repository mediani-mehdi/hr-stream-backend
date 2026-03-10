package com.medev.hrstream.candidate;

/**
 * Response DTO for CV operations.
 */
public record CvResponse(
        String objectKey,
        String filename,
        String contentType,
        Long sizeBytes,
        String presignedUrl
) {}
