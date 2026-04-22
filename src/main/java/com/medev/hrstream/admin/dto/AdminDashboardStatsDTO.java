package com.medev.hrstream.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private long totalUsers;
    private long totalCandidates;
    private long totalHr;
    private long totalAdmins;
    private long activeUsers;
    private long newUsersThisMonth;
}
