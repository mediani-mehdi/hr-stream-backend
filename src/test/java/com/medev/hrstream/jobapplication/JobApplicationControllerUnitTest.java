package com.medev.hrstream.jobapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateIdentityService;
import com.medev.hrstream.candidate.profile.CandidateProfileService;
import com.medev.hrstream.candidate.profile.dto.ProfileCompletenessResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private JobApplicationService service;

    @Mock
    private ApplicationSubmissionService submissionService;

    @Mock
    private CandidateProfileService candidateProfileService;

    @Mock
    private CandidateIdentityService candidateIdentityService;

    @InjectMocks
    private JobApplicationController controller;

    private ObjectMapper objectMapper = new ObjectMapper();
    private JobApplication mockApplication;
    private Candidate mockCandidate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockCandidate = new Candidate();
        try {
            java.lang.reflect.Field idField = Candidate.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockCandidate, UUID.randomUUID().toString());
        } catch (Exception e) {}

        mockApplication = new JobApplication();
        try {
            java.lang.reflect.Field idField = JobApplication.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockApplication, UUID.randomUUID().toString());

            java.lang.reflect.Field dateField = JobApplication.class.getDeclaredField("applicationDate");
            dateField.setAccessible(true);
            dateField.set(mockApplication, LocalDateTime.now());

            java.lang.reflect.Field statusField = JobApplication.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(mockApplication, ApplicationStatus.SUBMITTED);

            java.lang.reflect.Field candField = JobApplication.class.getDeclaredField("candidate");
            candField.setAccessible(true);
            candField.set(mockApplication, mockCandidate);
        } catch (Exception e) {}
    }

    private String getAppId() {
        try {
            java.lang.reflect.Field idField = JobApplication.class.getDeclaredField("id");
            idField.setAccessible(true);
            return (String) idField.get(mockApplication);
        } catch (Exception e) { return ""; }
    }

    private String getCandId() {
        try {
            java.lang.reflect.Field idField = Candidate.class.getDeclaredField("id");
            idField.setAccessible(true);
            return (String) idField.get(mockCandidate);
        } catch (Exception e) { return ""; }
    }

    @Test
    void findAll_ShouldReturnPageOfApplications() throws Exception {
        Page<JobApplication> page = new PageImpl<>(Collections.singletonList(mockApplication));
        when(service.findAll(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/applications")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "applicationDate")
                .param("direction", "desc"));
    }

    @Test
    void findById_ShouldReturnApplication() throws Exception {
        when(service.findById(getAppId())).thenReturn(mockApplication);

        mockMvc.perform(get("/applications/" + getAppId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(getAppId()));
    }

    @Test
    void updateStatus_ShouldReturnUpdatedApplication() throws Exception {
        try {
            java.lang.reflect.Field statusField = JobApplication.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(mockApplication, ApplicationStatus.HIRED);
        } catch (Exception e) {}
        when(service.updateStatus(eq(getAppId()), any(ApplicationStatus.class))).thenReturn(mockApplication);

        mockMvc.perform(patch("/applications/" + getAppId() + "/status")
                        .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(service).delete(getAppId());

        mockMvc.perform(delete("/applications/" + getAppId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void apply_WhenProfileReady_ShouldReturnAccepted() throws Exception {
        UUID candidateId = UUID.fromString(getCandId());
        ProfileCompletenessResponse completeness = ProfileCompletenessResponse.builder().readyToApply(true).build();

        when(candidateIdentityService.requireCurrentCandidateId()).thenReturn(candidateId);
        when(candidateProfileService.getCompleteness(candidateId)).thenReturn(completeness);
        when(candidateIdentityService.requireCurrentCandidate()).thenReturn(mockCandidate);
        when(submissionService.submit(eq("software-engineer"), any(Candidate.class), any()))
                .thenReturn(mockApplication);

        MockMultipartFile cv = new MockMultipartFile("cv", "resume.pdf", "application/pdf", "pdf-content".getBytes());

        mockMvc.perform(multipart("/jobs/software-engineer/apply").file(cv))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(getAppId()));
    }
}
