package com.medev.hrstream.integration;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.integration.support.FakeAiChatClient;
import com.medev.hrstream.integration.support.TestPdfFixtures;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.ApplicationSubmissionService;
import com.medev.hrstream.jobapplication.JobApplication;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
class ScoringPipelineIntegrationTest extends BaseIntegrationTest {

    @Autowired ApplicationSubmissionService submission;
    @Autowired JobRepository jobs;
    @Autowired CandidateRepository candidates;
    @Autowired JobApplicationRepository applications;
    @Autowired FakeAiChatClient fakeAi;

    @BeforeEach
    void setup() { fakeAi.resetCalls(); }

    @Test
    void submittedApplicationCompletesPipelineAndStaysPending() {
        Job job = jobs.save(buildJob("Java Engineer", List.of("Java", "Spring")));
        Candidate candidate = candidates.save(buildCandidate("alice@test.com"));

        fakeAi.respondWithScore(85, "good match");

        JobApplication created = submission.submit(
                job.getApplicationToken(), candidate,
                TestPdfFixtures.wellFormed("cv"));

        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            JobApplication reloaded = applications.findById(created.getId()).orElseThrow();
            assertThat(reloaded.getPipelineStatus()).isEqualTo(PipelineStatus.DONE);
            assertThat(reloaded.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
            assertThat(reloaded.getAiScore()).isEqualTo(85);
            assertThat(reloaded.getAiProvider()).isEqualTo("fake");
            assertThat(reloaded.getProcessingErrorCode()).isNull();
        });
        assertThat(fakeAi.callCount()).isEqualTo(1);
    }

    @Test
    void lowAiScoreAutoRejects() {
        Job job = jobs.save(buildJob("Java Engineer", List.of("Java")));
        Candidate candidate = candidates.save(buildCandidate("bob@test.com"));

        fakeAi.respondWithScore(40, "weak fit");

        JobApplication created = submission.submit(
                job.getApplicationToken(), candidate,
                TestPdfFixtures.wellFormed("cv"));

        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            JobApplication reloaded = applications.findById(created.getId()).orElseThrow();
            assertThat(reloaded.getPipelineStatus()).isEqualTo(PipelineStatus.DONE);
            assertThat(reloaded.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
            assertThat(reloaded.getAiScore()).isEqualTo(40);
        });
    }

    @Test
    void lowRuleScoreSkipsAiAndRejects() {
        Job job = jobs.save(buildJob("Rust Engineer", List.of("Rust", "Tokio", "Async")));
        Candidate candidate = candidates.save(buildCandidate("carol@test.com"));

        JobApplication created = submission.submit(
                job.getApplicationToken(), candidate,
                TestPdfFixtures.wellFormed("cv"));    // CV mentions Java, not Rust

        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            JobApplication reloaded = applications.findById(created.getId()).orElseThrow();
            assertThat(reloaded.getPipelineStatus()).isEqualTo(PipelineStatus.DONE);
            assertThat(reloaded.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
            assertThat(reloaded.getAiScore()).isNull();
        });
        assertThat(fakeAi.callCount()).isZero();
    }

    @Test
    void emptyCvMarksFailed() {
        Job job = jobs.save(buildJob("Java Engineer", List.of("Java")));
        Candidate candidate = candidates.save(buildCandidate("dan@test.com"));

        JobApplication created = submission.submit(
                job.getApplicationToken(), candidate,
                TestPdfFixtures.imageOnly("cv"));

        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            JobApplication reloaded = applications.findById(created.getId()).orElseThrow();
            assertThat(reloaded.getPipelineStatus()).isEqualTo(PipelineStatus.FAILED);
            assertThat(reloaded.getProcessingErrorCode()).isNotNull();
        });
    }

    @Test
    void aiProvidersExhaustedMarksFailed() {
        Job job = jobs.save(buildJob("Java Engineer", List.of("Java")));
        Candidate candidate = candidates.save(buildCandidate("eve@test.com"));

        fakeAi.failAll();

        JobApplication created = submission.submit(
                job.getApplicationToken(), candidate,
                TestPdfFixtures.wellFormed("cv"));

        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            JobApplication reloaded = applications.findById(created.getId()).orElseThrow();
            assertThat(reloaded.getPipelineStatus()).isEqualTo(PipelineStatus.FAILED);
        });
    }

    private Job buildJob(String title, List<String> requiredSkills) {
        return Job.builder()
                .title(title)
                .description("Build stuff")
                .requiredSkills(requiredSkills)
                .niceToHaveSkills(List.of())
                .experienceLevel("senior")
                .status(JobStatus.OPEN)
                .applicationToken(UUID.randomUUID().toString())
                .deleted(false)
                .build();
    }

    private Candidate buildCandidate(String email) {
        return Candidate.builder()
                .firstName("F").lastName("L").email(email).phone("000")
                .password("pw12345")
                .build();
    }
}
