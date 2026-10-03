package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;
import com.hrishabh.problemservice.dailychallenge.planner.PotdDifficulty;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DailyChallengePlannerTest {

    private DailyChallengePlanner planner;
    private PotdSchedulerProperties properties;

    @BeforeEach
    void setUp() {
        properties = new PotdSchedulerProperties();
        properties.setExactCooldownDays(7);
        properties.setEmergencyMinCooldownDays(2);
        planner = new DailyChallengePlanner(properties);
    }

    @Test
    void plan_prefersHigherQualityWhenCooldownAllows() {
        LocalDate d1 = LocalDate.of(2026, 2, 1);
        LocalDate d2 = LocalDate.of(2026, 2, 2);
        PotdPlanningModels.PotdPlanningCandidate low = candidate(1L, 0.2);
        PotdPlanningModels.PotdPlanningCandidate high = candidate(2L, 0.9);

        PotdPlanningModels.PlannerResult result = planner.plan(new PotdPlanningModels.PlannerRequest(
                d1, d2, List.of(d1, d2), List.of(low, high), List.of(), Map.of()));

        assertThat(result.assignments()).hasSize(2);
        assertThat(result.assignments().getFirst().questionId()).isEqualTo(2L);
    }

    @Test
    void plan_respectsAdjacentDayCooldown() {
        LocalDate d1 = LocalDate.of(2026, 3, 1);
        LocalDate d2 = LocalDate.of(2026, 3, 2);
        PotdPlanningModels.PotdPlanningCandidate only = candidate(1L, 0.5);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> planner.plan(
                new PotdPlanningModels.PlannerRequest(
                        d1, d2, List.of(d1, d2), List.of(only), List.of(), Map.of())));
    }

    private static PotdPlanningModels.PotdPlanningCandidate candidate(long id, double quality) {
        return new PotdPlanningModels.PotdPlanningCandidate(
                id,
                PotdDifficulty.MEDIUM,
                10L,
                Map.of(10L, 1.0),
                "family-" + id,
                quality,
                true);
    }
}
