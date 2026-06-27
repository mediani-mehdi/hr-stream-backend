package com.medev.hrstream.AimodelService.lmstudio;

import com.medev.hrstream.config.ApplicationProperties;
import com.medev.hrstream.job.Job;
import com.medev.hrstream.job.JobRepository;
import com.medev.hrstream.job.JobResponseDTO;
import com.medev.hrstream.job.JobStatus;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LMStudioService {


    private final OpenAiChatModel chatModel;
    private final String modelName;
    private final JobRepository jobRepository;
    private final ApplicationProperties applicationProperties;

    public LMStudioService(@Qualifier("lmStudioChatModel") OpenAiChatModel chatModel,
                           @Qualifier("lmStudioModelName") String modelName,
                           JobRepository jobRepository,
                           ApplicationProperties applicationProperties) {
        this.chatModel = chatModel;
        this.modelName = modelName;
        this.jobRepository = jobRepository;
        this.applicationProperties = applicationProperties;
    }

    public JobResponseDTO generateJobDescription(Job job) {
        // Step 1: Save the job first to get an ID (with status = DRAFT)
        if (job.getStatus() == null) {
            job.setStatus(JobStatus.DRAFT);
        }
        if (job.getCreatedDate() == null) {
            job.setCreatedDate(LocalDateTime.now());
        }
        Job savedJob = jobRepository.save(job);

        // Step 2: Generate the application link and token if not already present
        if (savedJob.getApplicationToken() == null) {
            generateApplicationLink(savedJob);
            savedJob = jobRepository.save(savedJob);
        }

        // Step 3: Build AI prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("Act as an Expert HR Recruiter. Generate a professional, engaging, and complete job description in Markdown format.\n\n");

        prompt.append("Use the following details:\n");
        prompt.append("- **Job Title**: ").append(safeGet(savedJob.getTitle())).append("\n");
        if (savedJob.getLocation() != null && !savedJob.getLocation().isBlank()) {
            prompt.append("- **Location**: ").append(savedJob.getLocation()).append("\n");
        }
        if (savedJob.getExperienceLevel() != null && !savedJob.getExperienceLevel().isBlank()) {
            prompt.append("- **Experience Level**: ").append(savedJob.getExperienceLevel()).append("\n");
        }
        if (savedJob.getContractType() != null) {
            prompt.append("- **Contract Type**: ").append(savedJob.getContractType().name()).append("\n");
        }
        if (savedJob.getCompanyDetails() != null && !savedJob.getCompanyDetails().isBlank()) {
            prompt.append("- **Company**: ").append(savedJob.getCompanyDetails()).append("\n");
        }
        if (savedJob.getRequiredSkills() != null && !savedJob.getRequiredSkills().isEmpty()) {
            prompt.append("- **Required Skills**: ").append(String.join(", ", savedJob.getRequiredSkills())).append("\n");
        }
        if (savedJob.getNiceToHaveSkills() != null && !savedJob.getNiceToHaveSkills().isEmpty()) {
            prompt.append("- **Nice to Have Skills**: ").append(String.join(", ", savedJob.getNiceToHaveSkills())).append("\n");
        }
        if (savedJob.getAdditionalInfo() != null && !savedJob.getAdditionalInfo().isBlank()) {
            prompt.append("- **Additional Notes**: ").append(savedJob.getAdditionalInfo()).append("\n");
        }

        prompt.append("\n");
        prompt.append("Structure the output in clean Markdown with these sections:\n");
        prompt.append("## Role Overview\n");
        prompt.append("## Key Responsibilities (use bullet points)\n");
        prompt.append("## Required Qualifications & Skills (use bullet points)\n");
        prompt.append("## What We Offer / Why Join Us?\n\n");

        prompt.append("Tone: Professional, inclusive, and exciting. Avoid generic phrases. Be specific and compelling.\n");
        prompt.append("Do not include application instructions or links - those will be added separately.");

        try {
            // Step 4: Generate description using AI
            String description = chatModel.call(new Prompt(prompt.toString())).getResult().getOutput().getText();
            savedJob.setDescription(description);

            // Step 5: Save the job again with description and application link
            savedJob.setUpdatedDate(LocalDateTime.now());
            Job finalJob = jobRepository.save(savedJob);

            // Step 6: Return complete job data
            return JobResponseDTO.builder()
                    .id(finalJob.getId())
                    .title(finalJob.getTitle())
                    .description(finalJob.getDescription())
                    .applicationLink(finalJob.getApplyLink())
                    .applyUrl(finalJob.getApplyLink())
                    .status(finalJob.getStatus())
                    .location(finalJob.getLocation())
                    .experienceLevel(finalJob.getExperienceLevel())
                    .employmentType(finalJob.getContractType() != null ? finalJob.getContractType().name() : null)
                    .skills(finalJob.getRequiredSkills())
                    .createdAt(finalJob.getCreatedDate())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate job description using LM Studio", e);
        }
    }

    public String generateDescription(String prompt) {
        try {
            return chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate LM Studio response", e);
        }
    }

    private void generateApplicationLink(Job job) {
        // Generate unique secure token
        String token = UUID.randomUUID().toString();
        job.setApplicationToken(token);

        String baseUrl = applicationProperties.getBaseUrl();
        String applyLink = String.format("%s/apply/%s", baseUrl, token);
        job.setApplyLink(applyLink);
    }

    // Helper to avoid nulls
    private String safeGet(String value) {
        return value == null ? "" : value;
    }
}
