package com.medev.hrstream.integration;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.integration.support.FakeAiChatClient;
import com.medev.hrstream.integration.support.TestPdfFixtures;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.jobapplication.ApplicationSubmissionService;
import com.medev.hrstream.jobapplication.ApplicationStatus;
import com.medev.hrstream.jobapplication.JobApplicationRepository;
import com.medev.hrstream.jobapplication.scoring.PipelineStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@TestPropertySource(properties = "app.scoring.max-applications=5")
class ConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired ApplicationSubmissionService submission;
    @Autowired JobRepository jobs;
    @Autowired CandidateRepository candidates;
    @Autowired JobApplicationRepository applications;
    @Autowired FakeAiChatClient fakeAi;

    @Test
    void parallelSubmissionsCloseJobAfterCap() throws Exception {
        fakeAi.respondWithScore(90, "ok");
        Job job = jobs.save(openJob());
        List<Candidate> people = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            people.add(candidates.save(Candidate.builder()
                    .firstName("F").lastName("L").email("p" + i + "@x.com").phone("0")
                    .password("pw12345")
                    .build()));
        }

        ExecutorService pool = Executors.newFixedThreadPool(5);
        CountDownLatch start = new CountDownLatch(1);
        for (Candidate c : people) {
            pool.submit(() -> {
                start.await();
                try {
                    submission.submit(job.getApplicationToken(), c, TestPdfFixtures.wellFormed("cv"));
                } catch (Exception ignored) { /* duplicate / closed are fine here */ }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Job reloaded = jobs.findById(job.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(JobStatus.FILLED);
        });

        long nonRejectedCount = applications.findAll().stream()
                .filter(a -> a.getJob().getId().equals(job.getId()))
                .filter(a -> a.getStatus() != ApplicationStatus.REJECTED)
                .filter(a -> a.getPipelineStatus() == PipelineStatus.DONE)
                .count();
        assertThat(nonRejectedCount).isLessThanOrEqualTo(5);
    }

    private Job openJob() {
        return Job.builder()
                .title("Hot Role").description("d")
                .requiredSkills(List.of("Java"))
                .niceToHaveSkills(List.of())
                .experienceLevel("senior")
                .status(JobStatus.OPEN)
                .applicationToken(UUID.randomUUID().toString())
                .deleted(false).build();
    }
}
