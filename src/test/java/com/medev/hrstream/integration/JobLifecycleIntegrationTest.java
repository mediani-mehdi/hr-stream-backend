package com.medev.hrstream.integration;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.integration.support.FakeAiChatClient;
import com.medev.hrstream.integration.support.TestPdfFixtures;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.job.lifecycle.ClosedReason;
import com.medev.hrstream.job.lifecycle.JobLifecycleService;
import com.medev.hrstream.jobapplication.ApplicationSubmissionService;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@TestPropertySource(properties = "app.scoring.max-applications=2")
class JobLifecycleIntegrationTest extends BaseIntegrationTest {

    @Autowired ApplicationSubmissionService submission;
    @Autowired JobRepository jobs;
    @Autowired CandidateRepository candidates;
    @Autowired JobApplicationRepository applications;
    @Autowired JobLifecycleService lifecycle;
    @Autowired FakeAiChatClient fakeAi;

    @Test
    void capReachedClosesJobAutomatically() {
        fakeAi.respondWithScore(90, "ok");
        Job job = jobs.save(openJob("Java Engineer", LocalDateTime.now().plusDays(7)));
        Candidate c1 = candidates.save(cand("c1@x.com"));
        Candidate c2 = candidates.save(cand("c2@x.com"));

        submission.submit(job.getApplicationToken(), c1, TestPdfFixtures.wellFormed("cv"));
        submission.submit(job.getApplicationToken(), c2, TestPdfFixtures.wellFormed("cv"));

        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Job reloaded = jobs.findById(job.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(JobStatus.FILLED);
            assertThat(reloaded.getClosedReason()).isEqualTo(ClosedReason.CAP_REACHED);
            assertThat(reloaded.getClosedAt()).isNotNull();
        });
    }

    @Test
    void deadlineSweepClosesExpiredJob() {
        Job past = jobs.save(openJob("Role A", LocalDateTime.now().minusMinutes(1)));
        Job future = jobs.save(openJob("Role B", LocalDateTime.now().plusDays(1)));

        int closed = lifecycle.closeExpiredJobs(LocalDateTime.now());

        assertThat(closed).isEqualTo(1);
        assertThat(jobs.findById(past.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.FILLED);
        assertThat(jobs.findById(past.getId()).orElseThrow().getClosedReason()).isEqualTo(ClosedReason.DEADLINE_PASSED);
        assertThat(jobs.findById(future.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.OPEN);
    }

    private Job openJob(String title, LocalDateTime deadline) {
        return Job.builder()
                .title(title).description("d")
                .requiredSkills(List.of("Java"))
                .niceToHaveSkills(List.of())
                .experienceLevel("senior")
                .status(JobStatus.OPEN)
                .applicationToken(UUID.randomUUID().toString())
                .dateLimte(deadline)
                .deleted(false).build();
    }

    private Candidate cand(String email) {
        return Candidate.builder()
                .firstName("F").lastName("L").email(email).phone("0")
                .password("pw12345")
                .build();
    }
}
