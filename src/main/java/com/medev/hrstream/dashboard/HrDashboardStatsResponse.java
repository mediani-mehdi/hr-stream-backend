package com.medev.hrstream.dashboard;

import com.medev.hrstream.jobapplication.JobApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrDashboardStatsResponse {
    private long totalJobs;
    private long totalCandidates;
    private long totalApplications;
    private long openJobsCount;
    private long closedJobsCount;
    private long hiredCount;
    private long rejectedCount;
    private List<JobApplication> latestApplications;
}
