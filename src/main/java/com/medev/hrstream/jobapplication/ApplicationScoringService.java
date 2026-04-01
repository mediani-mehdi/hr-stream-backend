package com.medev.hrstream.jobapplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medev.hrstream.Gemini.GeminiService;
import com.medev.hrstream.candidate.profile.CandidateProfileService;
import com.medev.hrstream.candidate.profile.dto.CandidateProfileResponse;
import com.medev.hrstream.job.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApplicationScoringService {

    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileService candidateProfileService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public ApplicationScoringService(JobApplicationRepository jobApplicationRepository,
                                     CandidateProfileService candidateProfileService,
                                     GeminiService geminiService,
                                     ObjectMapper objectMapper) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.candidateProfileService = candidateProfileService;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    @Async
    public void scoreApplicationAsync(UUID applicationId) {
        try {
            JobApplication application = jobApplicationRepository.findById(applicationId.toString())
                    .orElseThrow(() -> new IllegalArgumentException("Application not found"));

            CandidateProfileResponse profile = candidateProfileService.getProfile(UUID.fromString(application.getCandidate().getId()));
            Job job = application.getJob();

            String prompt = buildPrompt(profile, job);
            String response = geminiService.generateJsonResponse(prompt);
            applyScore(application, response);
        } catch (Exception ex) {
            log.error("Failed to score application {}", applicationId, ex);
        }
    }

    private void applyScore(JobApplication application, String response) {
        try {
            JsonNode node = objectMapper.readTree(response);
            int score = node.path("score").asInt(0);
            String reasoning = node.path("reasoning").asText("Score not available");
            application.setScore(score);
            application.setScoreReasoning(reasoning);
            jobApplicationRepository.save(application);
        } catch (Exception e) {
            log.error("Unable to parse scoring response for application {}", application.getId(), e);
        }
    }

    private String buildPrompt(CandidateProfileResponse profile, Job job) {
        String experiences = profile.getExperience().stream()
                .map(exp -> String.format("- %s at %s (%s to %s)%s",
                        safe(exp.getTitle()),
                        safe(exp.getCompany()),
                        exp.getStartDate(),
                        exp.getEndDate(),
                        exp.isCurrent() ? " [current]" : ""))
                .collect(Collectors.joining("\n"));

        String skills = profile.getSkills().stream()
                .map(skill -> String.format("- %s (%s)", skill.getName(), skill.getLevel()))
                .collect(Collectors.joining("\n"));

        String education = profile.getEducation().stream()
                .map(ed -> String.format("- %s, %s (%s-%s)",
                        safe(ed.getInstitution()),
                        safe(ed.getDegree()),
                        ed.getStartYear(),
                        ed.getEndYear()))
                .collect(Collectors.joining("\n"));

        return "You are an ATS scoring assistant. Compare the candidate to the job and respond with JSON {\"score\":0-100,\"reasoning\":\"two sentences\"}." +
                "\nJob Title: " + safe(job.getTitle()) +
                "\nJob Description:\n" + safe(job.getDescription()) +
                "\n\nCandidate Headline: " + safe(profile.getHeadline()) +
                "\nSummary: " + safe(profile.getSummary()) +
                "\n\nExperience:\n" + experiences +
                "\n\nSkills:\n" + skills +
                "\n\nEducation:\n" + education;
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }
}
