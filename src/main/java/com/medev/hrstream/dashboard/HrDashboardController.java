package com.medev.hrstream.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@PreAuthorize("hasRole('HR')")
public class HrDashboardController {

    private final HrDashboardService service;

    public HrDashboardController(HrDashboardService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public ResponseEntity<HrDashboardStatsResponse> getStats() {
        return ResponseEntity.ok(service.getStats());
    }
}
