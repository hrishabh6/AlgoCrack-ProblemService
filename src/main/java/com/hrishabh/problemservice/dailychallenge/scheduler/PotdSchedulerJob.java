package com.hrishabh.problemservice.dailychallenge.scheduler;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;
import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengeAdminDtos.GenerationRequest;
import com.hrishabh.problemservice.dailychallenge.service.DailyChallengeAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.time.Clock;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(prefix = "potd.scheduler", name = "enabled", havingValue = "true")
public class PotdSchedulerJob {

    private static final Logger log = LoggerFactory.getLogger(PotdSchedulerJob.class);

    private final PotdSchedulerProperties properties;
    private final DailyChallengeAdminService adminService;
    private final PotdMysqlSchedulerLock schedulerLock;
    private final Clock clock;

    public PotdSchedulerJob(
            PotdSchedulerProperties properties,
            DailyChallengeAdminService adminService,
            PotdMysqlSchedulerLock schedulerLock,
            Clock clock) {
        this.properties = properties;
        this.adminService = adminService;
        this.schedulerLock = schedulerLock;
        this.clock = clock;
    }

    @Scheduled(cron = "${potd.scheduler.cron}", zone = "${potd.scheduler.zone-id}")
    public void maintainDraftHorizon() {
        Connection lockConnection = null;
        try {
            lockConnection = schedulerLock.tryAcquire();
            if (lockConnection == null) {
                log.info("POTD scheduler skipped: lock held by another instance");
                return;
            }
            LocalDate today = LocalDate.now(clock);
            LocalDate from = today.plusDays(1);
            LocalDate to = today.plusDays(properties.getDraftHorizonDays());
            GenerationRequest request = new GenerationRequest(from, to, false, false);
            var result = adminService.generate(request, "potd-scheduler");
            log.info(
                    "POTD scheduler generated {} draft rows (planned={})",
                    result.getPersistedCount(),
                    result.getPlanned().size());
        } catch (Exception ex) {
            log.warn("POTD scheduler run failed: {}", ex.getMessage());
        } finally {
            schedulerLock.release(lockConnection);
        }
    }
}
