package com.medev.hrstream.job.lifecycle;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class JobAutoCloseScheduler {

    private final JobLifecycleService lifecycle;

    public JobAutoCloseScheduler(JobLifecycleService lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Scheduled(cron = "${app.scheduling.deadline-check-cron:0 */5 * * * *}")
    @SchedulerLock(name = "jobAutoCloseScheduler", lockAtLeastFor = "PT10S", lockAtMostFor = "PT4M")
    public void sweep() {
        int closed = lifecycle.closeExpiredJobs(LocalDateTime.now());
        if (closed > 0) {
            log.info("scheduler: closed {} expired jobs", closed);
        }
    }
}
