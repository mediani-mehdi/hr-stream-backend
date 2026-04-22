package com.medev.hrstream.admin;

import com.medev.hrstream.admin.dto.AdminDashboardStatsDTO;
import com.medev.hrstream.admin.dto.CreateHrUserRequest;
import com.medev.hrstream.admin.dto.UserStatusRequest;
import com.medev.hrstream.admin.dto.UserSummaryDTO;
import com.medev.hrstream.common.PageResponse;
import com.medev.hrstream.user.Role;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users/hr")
    public ResponseEntity<UserSummaryDTO> createHrUser(@RequestBody CreateHrUserRequest request) {
        return ResponseEntity.ok(adminService.createHrUser(request));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserSummaryDTO>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<UserSummaryDTO> result = adminService.listUsers(role, page, size, sortBy, direction);
        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        ));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserSummaryDTO> toggleStatus(
            @PathVariable String id,
            @RequestBody UserStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.toggleStatus(id, request.isActive()));
    }
}
