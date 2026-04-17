package com.medev.hrstream.integration;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
import com.medev.hrstream.integration.support.TestPdfFixtures;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobStatus;
import com.medev.hrstream.jobapplication.ApplicationSubmissionService;
import com.medev.hrstream.jobapplication.submission.CvFileRejectedException;
import com.medev.hrstream.jobapplication.submission.DuplicateApplicationException;
import com.medev.hrstream.jobapplication.submission.InvalidApplicationTokenException;
import com.medev.hrstream.jobapplication.submission.JobClosedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("integration-test")
class SubmissionIntegrationTest extends BaseIntegrationTest {

    @Autowired ApplicationSubmissionService submission;
    @Autowired JobRepository jobs;
    @Autowired CandidateRepository candidates;

    @Test
    void unknownTokenThrowsInvalidApplicationToken() {
        Candidate candidate = candidates.save(cand("x1@x.com"));
        assertThatThrownBy(() -> submission.submit(
                "bogus-token", candidate, TestPdfFixtures.wellFormed("cv")))
                .isInstanceOf(InvalidApplicationTokenException.class);
    }

    @Test
    void closedJobThrowsJobClosed() {
        Job job = jobs.save(openJob(JobStatus.FILLED));
        Candidate candidate = candidates.save(cand("x2@x.com"));
        assertThatThrownBy(() -> submission.submit(
                job.getApplicationToken(), candidate, TestPdfFixtures.wellFormed("cv")))
                .isInstanceOf(JobClosedException.class);
    }

    @Test
    void duplicateApplicationThrows() {
        Job job = jobs.save(openJob(JobStatus.OPEN));
        Candidate candidate = candidates.save(cand("x3@x.com"));

        submission.submit(job.getApplicationToken(), candidate, TestPdfFixtures.wellFormed("cv"));

        assertThatThrownBy(() -> submission.submit(
                job.getApplicationToken(), candidate, TestPdfFixtures.wellFormed("cv")))
                .isInstanceOf(DuplicateApplicationException.class);
    }

    @Test
    void missingCvIsRejected() {
        Job job = jobs.save(openJob(JobStatus.OPEN));
        Candidate candidate = candidates.save(cand("x4@x.com"));
        MockMultipartFile empty = new MockMultipartFile("cv", "empty.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> submission.submit(job.getApplicationToken(), candidate, empty))
                .isInstanceOf(CvFileRejectedException.class);
    }

    @Test
    void wrongContentTypeIsRejected() {
        Job job = jobs.save(openJob(JobStatus.OPEN));
        Candidate candidate = candidates.save(cand("x5@x.com"));
        MockMultipartFile bad = new MockMultipartFile("cv", "virus.exe", "application/octet-stream",
                "MZ".getBytes());
        assertThatThrownBy(() -> submission.submit(job.getApplicationToken(), candidate, bad))
                .isInstanceOf(CvFileRejectedException.class);
    }

    private Job openJob(JobStatus status) {
        return Job.builder()
                .title("Role").description("d")
                .requiredSkills(List.of("Java"))
                .niceToHaveSkills(List.of())
                .experienceLevel("senior")
                .status(status)
                .applicationToken(UUID.randomUUID().toString())
                .deleted(false).build();
    }

    private Candidate cand(String email) {
        return Candidate.builder()
                .firstName("F").lastName("L").email(email).phone("0")
                .password("pw12345")
                .build();
    }
}
