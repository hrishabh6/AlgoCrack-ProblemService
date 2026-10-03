package com.hrishabh.problemservice.dailychallenge.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "potd.scheduler")
public class PotdSchedulerProperties {

    /** When false, the scheduled job is a no-op (local/test default). */
    private boolean enabled = false;

    @NotBlank
    private String zoneId = "UTC";

    /** Quartz-style cron for rolling horizon maintenance (default: daily 15:00 UTC). */
    @NotBlank
    private String cron = "0 0 15 * * ?";

    @Min(1)
    @Max(366)
    private int draftHorizonDays = 45;

    @Min(1)
    private int publishedLowWaterDays = 14;

    @Min(1)
    private int publishedWarnDays = 21;

    @Min(1)
    @Max(366)
    private int maxPlanningHorizonDays = 90;

    @Min(1)
    private int historyWindowDays = 120;

    @Min(1)
    private int exactCooldownDays = 14;

    @Min(1)
    private int emergencyMinCooldownDays = 3;

    @Min(1)
    private int familyCooldownDays = 30;

    @NotBlank
    private String schedulerVersion = "potd-planner-v1";

    @NotBlank
    private String mysqlLockName = "potd_scheduler_job";

    @Min(1)
    @Max(60)
    private int lockAcquisitionTimeoutSeconds = 5;

    @Valid
    @NotNull
    private ScoringWeights scoring = new ScoringWeights();

    @Valid
    @NotNull
    private DifficultyTargets difficulty = new DifficultyTargets();

    @Getter
    @Setter
    public static class ScoringWeights {
        @Min(0)
        private double quality = 0.25;
        @Min(0)
        private double difficultyDeficit = 0.20;
        @Min(0)
        private double primaryTopicDeficit = 0.18;
        @Min(0)
        private double freshness = 0.15;
        @Min(0)
        private double neverUsed = 0.07;
        @Min(0)
        private double recentTagSimilarity = 0.08;
        @Min(0)
        private double primaryTopicRecency = 0.04;
        @Min(0)
        private double difficultyStreak = 0.02;
        @Min(0)
        private double overTarget = 0.01;
    }

    @Getter
    @Setter
    public static class DifficultyTargets {
        @Min(0)
        @Max(1)
        private double easyRatio = 0.20;
        @Min(0)
        @Max(1)
        private double mediumRatio = 0.60;
        @Min(0)
        @Max(1)
        private double hardRatio = 0.20;
        @Min(0)
        private int tolerancePer30Days = 1;
    }
}
