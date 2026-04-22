package com.medev.hrstream.admin;

import com.medev.hrstream.admin.dto.AdminDashboardStatsDTO;
import com.medev.hrstream.admin.dto.CreateHrUserRequest;
import com.medev.hrstream.admin.dto.UserSummaryDTO;
import com.medev.hrstream.user.Role;
import com.medev.hrstream.user.User;
import com.medev.hrstream.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserSummaryDTO createHrUser(CreateHrUserRequest request) {
        User user = new User();
        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.HR);
        user.setIsActive(true);
        user.setIsDeleted(false);

        User saved = userService.save(user);
        return toSummary(saved);
    }

    public AdminDashboardStatsDTO getStats() {
        AdminDashboardStatsDTO stats = new AdminDashboardStatsDTO();
        stats.setTotalUsers(userService.countAll());
        stats.setTotalCandidates(userService.countByRole(Role.CANDIDATE));
        stats.setTotalHr(userService.countByRole(Role.HR));
        stats.setTotalAdmins(userService.countByRole(Role.ADMIN));
        stats.setActiveUsers(userService.countActiveUsers());
        stats.setNewUsersThisMonth(userService.countNewUsersThisMonth());
        return stats;
    }

    public Page<UserSummaryDTO> listUsers(Role role, int page, int size, String sortBy, String direction) {
        return userService.findAll(role, page, size, sortBy, direction).map(this::toSummary);
    }

    public UserSummaryDTO toggleStatus(String id, boolean active) {
        User user = userService.findById(id).orElseThrow();
        user.setIsActive(active);
        return toSummary(userService.save(user));
    }

    private UserSummaryDTO toSummary(User user) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstname());
        dto.setLastName(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
