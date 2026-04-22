package com.medev.hrstream.jobapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @JsonIgnoreProperties({"education", "experience", "skills", "languages", "password", "resumeObjectKey", "resumeUrl"})
    private Candidate candidate;

    @CreationTimestamp
    private LocalDateTime applicationDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    // --- Pipeline state ---
    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_status", nullable = false)
    @Builder.Default
    private PipelineStatus pipelineStatus = PipelineStatus.QUEUED;

    @Column(name = "cv_blob_key", length = 512)
    private String cvBlobKey;

    @Column(name = "cv_extracted_chars")
    private Integer cvExtractedChars;

    // --- Scoring output ---
    @Column(name = "rule_score")
    private Integer ruleScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_score_details", columnDefinition = "jsonb")
    private Map<String, Object> ruleScoreDetails;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(name = "ai_reasoning", columnDefinition = "TEXT")
    private String aiReasoning;

    @Column(name = "ai_provider", length = 32)
    private String aiProvider;

    // --- Error diagnostics ---
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_error_code", length = 64)
    private ProcessingErrorCode processingErrorCode;

    @Column(name = "processing_error_message", columnDefinition = "TEXT")
    private String processingErrorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "pipeline_attempt_count", nullable = false)
    @Builder.Default
    private Integer pipelineAttemptCount = 0;

    @Column(name = "pipeline_last_attempt_at")
    private LocalDateTime pipelineLastAttemptAt;
}
