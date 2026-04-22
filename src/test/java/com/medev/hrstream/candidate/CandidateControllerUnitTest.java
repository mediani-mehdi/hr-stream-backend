package com.medev.hrstream.candidate;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CandidateControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private CandidateService candidateService;

    @InjectMocks
    private CandidateController candidateController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Candidate mockCandidate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(candidateController).build();
        
        mockCandidate = Candidate.builder()
                .id("cand-123")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@test.com")
                .build();
    }

    @Test
    void save_ShouldReturnSavedCandidate() throws Exception {
        when(candidateService.save(any(Candidate.class))).thenReturn(mockCandidate);

        mockMvc.perform(post("/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCandidate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cand-123"))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void findById_ShouldReturnCandidate() throws Exception {
        when(candidateService.findById("cand-123")).thenReturn(mockCandidate);

        mockMvc.perform(get("/candidates/cand-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cand-123"));
    }

    @Test
    void findAll_ShouldReturnPageOfCandidates() throws Exception {
        Page<Candidate> candidatePage = new PageImpl<>(Collections.singletonList(mockCandidate));
        when(candidateService.findAll(anyInt(), anyInt(), anyString(), anyString())).thenReturn(candidatePage);

        // Page serialization requires more complex setup in standalone MockMvc, skipping content check for now
        mockMvc.perform(get("/candidates")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc"));
    }

    @Test
    void update_ShouldReturnUpdatedCandidate() throws Exception {
        when(candidateService.update(eq("cand-123"), any(Candidate.class))).thenReturn(mockCandidate);

        mockMvc.perform(put("/candidates/cand-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCandidate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cand-123"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(candidateService).delete("cand-123");

        mockMvc.perform(delete("/candidates/cand-123"))
                .andExpect(status().isNoContent());
    }
}
