package com.medev.hrstream.jobapplication.scoring;

import com.medev.hrstream.config.ScoringProperties;
import com.medev.hrstream.file.MinioProperties;
import com.medev.hrstream.file.ResumeStorageService;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.JobApplication;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import com.medev.hrstream.jobapplication.events.JobApplicationFinalized;
import com.medev.hrstream.jobapplication.scoring.aiscorer.AiDeepScorer;
import com.medev.hrstream.jobapplication.scoring.aiscorer.AiScoreResult;
import com.medev.hrstream.jobapplication.scoring.aiscorer.AiScorerInput;
import com.medev.hrstream.jobapplication.scoring.aiscorer.AiScoringFailedException;
import com.medev.hrstream.jobapplication.scoring.domain.ScoringContext;
import com.medev.hrstream.jobapplication.scoring.extractor.CvTextExtractor;
import com.medev.hrstream.jobapplication.scoring.extractor.ExtractionFailedException;
import com.medev.hrstream.jobapplication.scoring.extractor.ExtractionResult;
import com.medev.hrstream.jobapplication.scoring.rulescorer.RuleScoreResult;
import com.medev.hrstream.jobapplication.scoring.rulescorer.RuleScorer;
import com.medev.hrstream.jobapplication.scoring.rulescorer.RuleScorerInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ScoringPipelineOrchestrator {

    private final JobApplicationRepository applications;
    private final ResumeStorageService storage;
    private final CvTextExtractor extractor;
    private final RuleScorer ruleScorer;
    private final AiDeepScorer aiScorer;
    private final ScoringProperties props;
    private final ApplicationEventPublisher events;
    private final S3Client s3Client;
    private final MinioProperties minioProperties;

    public ScoringPipelineOrchestrator(JobApplicationRepository applications,
                                       ResumeStorageService storage,
                                       CvTextExtractor extractor,
                                       RuleScorer ruleScorer,
                                       AiDeepScorer aiScorer,
                                       ScoringProperties props,
                                       ApplicationEventPublisher events,
                                       S3Client s3Client,
                                       MinioProperties minioProperties) {
        this.applications = applications;
        this.storage = storage;
        this.extractor = extractor;
        this.ruleScorer = ruleScorer;
        this.aiScorer = aiScorer;
        this.props = props;
        this.events = events;
        this.s3Client = s3Client;
        this.minioProperties = minioProperties;
    }

    /** Entry point, invoked by ApplicationSubmissionService after commit. */
    @Async("scoringExecutor")
    public void processAsync(ScoringContext ctx) {
        try {
            process(ctx);
        } catch (RuntimeException ex) {
            log.error("pipeline: unhandled failure for {}", ctx.getApplicationId(), ex);
            markFailed(ctx.getApplicationId(), ProcessingErrorCode.UNEXPECTED, ex.getMessage());
        }
    }

    void process(ScoringContext ctx) {
        transitionTo(ctx.getApplicationId(), PipelineStatus.EXTRACTING);
        ExtractionResult extraction;
        try (InputStream in = openCv(ctx.getCvBlobKey())) {
            extraction = extractor.extract(in);
        } catch (ExtractionFailedException e) {
            markFailed(ctx.getApplicationId(), e.getCode(), e.getMessage());
            return;
        } catch (Exception e) {
            markFailed(ctx.getApplicationId(), ProcessingErrorCode.CV_CORRUPTED, "could not read CV blob: " + e.getMessage());
            return;
        }

        if (extraction.getCharCount() < props.getMinExtractedChars()) {
            markFailed(ctx.getApplicationId(), ProcessingErrorCode.CV_EMPTY,
                    "CV had " + extraction.getCharCount() + " chars (min " + props.getMinExtractedChars() + ")");
            return;
        }

        transitionTo(ctx.getApplicationId(), PipelineStatus.RULE_SCORING);
        JobApplication app = loadForPipeline(ctx.getApplicationId());
        Job job = app.getJob();

        RuleScoreResult ruleResult;
        try {
            ruleResult = ruleScorer.score(RuleScorerInput.builder()
                    .cvText(extraction.getText())
                    .requiredSkills(job.getRequiredSkills())
                    .niceToHaveSkills(job.getNiceToHaveSkills())
                    .experienceLevel(job.getExperienceLevel())
                    .build());
        } catch (RuntimeException e) {
            markFailed(ctx.getApplicationId(), ProcessingErrorCode.RULE_SCORER_ERROR, e.getMessage());
            return;
        }

        saveExtractionAndRule(ctx.getApplicationId(), extraction.getCharCount(), ruleResult);

        if (ruleResult.getScore() < props.getRuleMinScore()) {
            finalizeRejected(ctx.getApplicationId(), ruleResult.getScore(), null, null,
                    "Below rule-score threshold (" + ruleResult.getScore() + " < " + props.getRuleMinScore() + ")");
            return;
        }

        transitionTo(ctx.getApplicationId(), PipelineStatus.AI_SCORING);
        AiScoreResult aiResult;
        try {
            aiResult = aiScorer.score(AiScorerInput.builder()
                    .jobTitle(job.getTitle())
                    .jobDescription(job.getDescription())
                    .requiredSkills(job.getRequiredSkills())
                    .niceToHaveSkills(job.getNiceToHaveSkills())
                    .experienceLevel(job.getExperienceLevel())
                    .cvText(extraction.getText())
                    .build());
        } catch (AiScoringFailedException e) {
            markFailed(ctx.getApplicationId(), e.getCode(), e.getMessage());
            return;
        } catch (RuntimeException e) {
            markFailed(ctx.getApplicationId(), ProcessingErrorCode.UNEXPECTED, e.getMessage());
            return;
        }

        if (aiResult.getScore() < props.getAiMinScore()) {
            finalizeRejected(ctx.getApplicationId(), ruleResult.getScore(),
                    aiResult.getScore(), aiResult.getProviderName(), aiResult.getReasoning());
        } else {
            finalizeSuccess(ctx.getApplicationId(), ruleResult.getScore(),
                    aiResult.getScore(), aiResult.getProviderName(), aiResult.getReasoning());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void transitionTo(String appId, PipelineStatus status) {
        JobApplication app = applications.findById(appId).orElseThrow();
        app.setPipelineStatus(status);
        app.setPipelineLastAttemptAt(LocalDateTime.now());
        app.setPipelineAttemptCount(app.getPipelineAttemptCount() + (status == PipelineStatus.EXTRACTING ? 1 : 0));
        applications.save(app);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected JobApplication loadForPipeline(String appId) {
        return applications.findById(appId).orElseThrow();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void saveExtractionAndRule(String appId, int chars, RuleScoreResult rule) {
        JobApplication app = applications.findById(appId).orElseThrow();
        app.setCvExtractedChars(chars);
        app.setRuleScore(rule.getScore());
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("requiredSkillsMatched", rule.getDetail().getRequiredSkillsMatched());
        detailMap.put("requiredSkillsTotal", rule.getDetail().getRequiredSkillsTotal());
        detailMap.put("matchedRequiredSkills", rule.getDetail().getMatchedRequiredSkills());
        detailMap.put("niceSkillsMatched", rule.getDetail().getNiceSkillsMatched());
        detailMap.put("niceSkillsTotal", rule.getDetail().getNiceSkillsTotal());
        detailMap.put("matchedNiceSkills", rule.getDetail().getMatchedNiceSkills());
        detailMap.put("experienceKeywordHits", rule.getDetail().getExperienceKeywordHits());
        app.setRuleScoreDetails(detailMap);
        applications.save(app);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void finalizeSuccess(String appId, int ruleScore, int aiScore, String provider, String reasoning) {
        JobApplication app = applications.findById(appId).orElseThrow();
        app.setAiScore(aiScore);
        app.setAiProvider(provider);
        app.setAiReasoning(reasoning);
        app.setStatus(ApplicationStatus.PENDING);
        app.setPipelineStatus(PipelineStatus.DONE);
        app.setProcessedAt(LocalDateTime.now());
        applications.save(app);
        publishFinal(app);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void finalizeRejected(String appId, int ruleScore, Integer aiScore, String provider, String reasoning) {
        JobApplication app = applications.findById(appId).orElseThrow();
        app.setAiScore(aiScore);
        app.setAiProvider(provider);
        app.setAiReasoning(reasoning);
        app.setStatus(ApplicationStatus.REJECTED);
        app.setPipelineStatus(PipelineStatus.DONE);
        app.setProcessedAt(LocalDateTime.now());
        applications.save(app);
        publishFinal(app);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void markFailed(String appId, ProcessingErrorCode code, String msg) {
        JobApplication app = applications.findById(appId).orElseThrow();
        app.setPipelineStatus(PipelineStatus.FAILED);
        app.setProcessingErrorCode(code);
        app.setProcessingErrorMessage(truncate(msg, 2000));
        app.setProcessedAt(LocalDateTime.now());
        applications.save(app);
        publishFinal(app);
    }

    private void publishFinal(JobApplication app) {
        events.publishEvent(JobApplicationFinalized.builder()
                .applicationId(app.getId())
                .jobId(app.getJob().getId())
                .candidateId(app.getCandidate().getId())
                .finalStatus(app.getStatus())
                .pipelineStatus(app.getPipelineStatus())
                .aiScore(app.getAiScore())
                .ruleScore(app.getRuleScore())
                .build());
    }

    private InputStream openCv(String objectKey) {
        var resp = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(minioProperties.getBucket())
                        .key(objectKey)
                        .build());
        return new ByteArrayInputStream(resp.asByteArray());
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
