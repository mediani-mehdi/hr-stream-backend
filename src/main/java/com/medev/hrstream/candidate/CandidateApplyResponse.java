package com.medev.hrstream.candidate;

import com.medev.hrstream.file.ResumeStorageService;
import com.medev.hrstream.jobapplication.JobApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplyResponse {
    private Candidate candidate;
    private ResumeStorageService.StoredObject resume;
    private JobApplication application;
}

