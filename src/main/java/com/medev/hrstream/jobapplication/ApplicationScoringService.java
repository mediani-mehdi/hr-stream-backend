package com.medev.hrstream.jobapplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medev.hrstream.Gemini.GeminiService;
import com.medev.hrstream.candidate.profile.CandidateProfileResponse;
import com.medev.hrstream.candidate.profile.CandidateProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class ApplicationScoringService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationScoringService.class);

    private final JobApplicationRepository applicationRepository;
    private final CandidateProfileService profileService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public ApplicationScoringService(
            JobApplicationRepository applicationRepository,
            CandidateProfileService profileService,
            GeminiService geminiService,
            ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.profileService = profileService;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    @Async
    @Transactional
    public void scoreApplicationAsync(String applicationId) {
        try {
            JobApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

            CandidateProfileResponse profile = profileService.getProfile(application.getCandidate().getId());
            String jobDescription = application.getJob().getDescription();

            String prompt = buildScoringPrompt(profile, jobDescription);
            String rawResponse = geminiService.generateContent(prompt);
            String json = extractJson(rawResponse);

            JsonNode node = objectMapper.readTree(json);
            int score = node.path("score").asInt(-1);
            String reasoning = node.path("reasoning").asText("");

            if (score >= 0 && score <= 100) {
                application.setScore(score);
                application.setScoreReasoning(reasoning);
                applicationRepository.save(application);
                log.info("Scored application {} with score {}", applicationId, score);
            } else {
                log.warn("Received invalid score {} for application {}, skipping persistence", score, applicationId);
            }
        } catch (Exception e) {
            log.error("Failed to score application {}: {}", applicationId, e.getMessage(), e);
        }
    }

    private String buildScoringPrompt(CandidateProfileResponse profile, String jobDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert HR recruiter. Score the following candidate profile against the job description.\n\n");

        sb.append("## Candidate Profile\n");
        if (profile.getHeadline() != null) sb.append("Headline: ").append(profile.getHeadline()).append("\n");
        if (profile.getSummary() != null) sb.append("Summary: ").append(profile.getSummary()).append("\n");

        if (profile.getExperience() != null && !profile.getExperience().isEmpty()) {
            sb.append("\n### Experience\n");
            profile.getExperience().forEach(exp ->
                    sb.append("- ").append(exp.getTitle()).append(" at ").append(exp.getCompany())
                            .append(exp.getDescription() != null ? ": " + exp.getDescription() : "")
                            .append("\n"));
        }

        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append("\n### Skills\n");
            String skills = profile.getSkills().stream()
                    .map(s -> s.getName() + " (" + s.getLevel() + ")")
                    .collect(Collectors.joining(", "));
            sb.append(skills).append("\n");
        }

        if (profile.getEducation() != null && !profile.getEducation().isEmpty()) {
            sb.append("\n### Education\n");
            profile.getEducation().forEach(edu ->
                    sb.append("- ").append(edu.getDegree()).append(" in ").append(edu.getFieldOfStudy())
                            .append(" from ").append(edu.getInstitution()).append("\n"));
        }

        if (profile.getLanguages() != null && !profile.getLanguages().isEmpty()) {
            sb.append("\n### Languages\n");
            String languages = profile.getLanguages().stream()
                    .map(l -> l.getLanguage() + " (" + l.getLevel() + ")")
                    .collect(Collectors.joining(", "));
            sb.append(languages).append("\n");
        }

        sb.append("\n## Job Description\n").append(jobDescription).append("\n\n");
        sb.append("Respond ONLY with a JSON object (no markdown, no code block) in this exact format:\n");
        sb.append("{\"score\": <integer 0-100>, \"reasoning\": \"<2-sentence explanation>\"}");

        return sb.toString();
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";
        // Strip markdown code fences if present
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end >= start) {
            return cleaned.substring(start, end + 1);
        }
        return "{}";
    }
}
