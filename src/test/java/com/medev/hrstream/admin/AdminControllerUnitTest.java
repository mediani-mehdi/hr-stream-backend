package com.medev.hrstream.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medev.hrstream.admin.dto.AdminDashboardStatsDTO;
import com.medev.hrstream.admin.dto.CreateHrUserRequest;
import com.medev.hrstream.admin.dto.UserSummaryDTO;
import com.medev.hrstream.user.Role;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void createHrUser_ShouldReturnCreatedHrUserDto() throws Exception {
        // Expected: POST /api/admin/users/hr returns the created HR summary.
        CreateHrUserRequest request = CreateHrUserRequest.builder()
                .firstName("Jane")
                .lastName("Recruiter")
                .email("jane@hrstream.local")
                .phoneNumber("123456789")
                .password("Password@123")
                .build();

        UserSummaryDTO response = new UserSummaryDTO();
        response.setId("hr-1");
        response.setFirstName("Jane");
        response.setLastName("Recruiter");
        response.setEmail("jane@hrstream.local");
        response.setRole(Role.HR);
        response.setIsActive(true);
        response.setCreatedAt(LocalDateTime.now());

        when(adminService.createHrUser(any(CreateHrUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/users/hr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("hr-1"))
                .andExpect(jsonPath("$.role").value("HR"));
    }

    @Test
    void getStats_ShouldReturnDashboardCounters() throws Exception {
        // Expected: GET /api/admin/dashboard/stats returns total counters from the users table.
        AdminDashboardStatsDTO stats = new AdminDashboardStatsDTO();
        stats.setTotalUsers(42);
        stats.setTotalCandidates(30);
        stats.setTotalHr(10);
        stats.setTotalAdmins(2);
        stats.setActiveUsers(38);
        stats.setNewUsersThisMonth(7);

        when(adminService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(42))
                .andExpect(jsonPath("$.newUsersThisMonth").value(7));
    }

    @Test
    void listUsers_ShouldReturnPagedUserSummaries() throws Exception {
        // Expected: GET /api/admin/users?role=HR returns a paged list of user summaries.
        UserSummaryDTO user = new UserSummaryDTO();
        user.setId("hr-1");
        user.setFirstName("Jane");
        user.setLastName("Recruiter");
        user.setEmail("jane@hrstream.local");
        user.setRole(Role.HR);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        Page<UserSummaryDTO> page = new PageImpl<>(Collections.singletonList(user));
        when(adminService.listUsers(eq(Role.HR), anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .param("role", "HR")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("jane@hrstream.local"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void toggleStatus_ShouldReturnUpdatedUserSummary() throws Exception {
        // Expected: PATCH /api/admin/users/{id}/status toggles active/inactive state.
        UserSummaryDTO response = new UserSummaryDTO();
        response.setId("hr-1");
        response.setFirstName("Jane");
        response.setLastName("Recruiter");
        response.setEmail("jane@hrstream.local");
        response.setRole(Role.HR);
        response.setIsActive(false);
        response.setCreatedAt(LocalDateTime.now());

        when(adminService.toggleStatus(eq("hr-1"), eq(false))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/hr-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("hr-1"))
                .andExpect(jsonPath("$.isActive").value(false));
    }
}
