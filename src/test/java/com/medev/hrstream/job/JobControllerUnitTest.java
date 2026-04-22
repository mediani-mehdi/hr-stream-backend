package com.medev.hrstream.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medev.hrstream.Gemini.GeminiService;
import com.medev.hrstream.common.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private JobController jobController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Job mockJob;
    private JobResponseDTO mockJobResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();

        mockJob = new Job();
        mockJob.setId("job-123");
        mockJob.setTitle("Software Engineer");
        mockJob.setDescription("Backend dev");
        mockJob.setStatus(JobStatus.DRAFT);

        mockJobResponse = JobResponseDTO.builder()
                .id("job-123")
                .title("Software Engineer")
                .description("Backend dev")
                .status(JobStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_ShouldReturnSavedJobId() throws Exception {
        when(jobService.save(any(Job.class))).thenReturn("job-123");

        mockMvc.perform(post("/jobs/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockJob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("job-123"));
    }

    @Test
    void publish_ShouldReturnSuccessMessage() throws Exception {
        when(jobService.publish("job-123", "OPEN")).thenReturn("Published Successfully");

        mockMvc.perform(post("/jobs/job-123/OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Published Successfully"));
    }

    @Test
    void updateStatus_ShouldReturnSuccessMessage() throws Exception {
        when(jobService.updateStatus("job-123", JobStatus.OPEN)).thenReturn("Status Updated");

        mockMvc.perform(patch("/jobs/job-123/status")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Status Updated"));
    }

    @Test
    void updateJob_ShouldReturnUpdatedJob() throws Exception {
        when(jobService.updateJob(eq("job-123"), any(Job.class))).thenReturn(mockJob);

        mockMvc.perform(put("/jobs/job-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockJob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("job-123"))
                .andExpect(jsonPath("$.title").value("Software Engineer"));
    }

    @Test
    void findById_ShouldReturnJob() throws Exception {
        when(jobService.findById("job-123")).thenReturn(mockJob);

        mockMvc.perform(get("/jobs/job-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("job-123"));
    }

    @Test
    void delete_ShouldReturnSuccessMessage() throws Exception {
        when(jobService.delete("job-123")).thenReturn("Deleted Successfully");

        mockMvc.perform(delete("/jobs/job-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Deleted Successfully"));
    }

    @Test
    void findAll_ShouldReturnPageOfJobs() throws Exception {
        Page<JobResponseDTO> jobPage = new PageImpl<>(Collections.singletonList(mockJobResponse));
        when(jobService.findAll(anyInt(), anyInt(), anyString(), anyString())).thenReturn(jobPage);

        mockMvc.perform(get("/jobs")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdDate")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("job-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}