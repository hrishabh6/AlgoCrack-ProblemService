package com.hrishabh.problemservice.dailychallenge.planner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class PotdPlanningModels {

    private PotdPlanningModels() {
    }

    public record PotdPlanningCandidate(
            long questionId,
            PotdDifficulty difficulty,
            Long primaryTagId,
            Map<Long, Double> tagWeights,
            String familyKey,
            double qualityNormalized,
            boolean neverScheduled) {
    }

    public record FixedAssignment(LocalDate date, long questionId, boolean locked) {
    }

    public record PlannedAssignment(
            LocalDate challengeDate,
            long questionId,
            BigDecimal selectionScore,
            String deterministicSeed) {
    }

    public record PlannerRequest(
            LocalDate planningStart,
            LocalDate planningEnd,
            List<LocalDate> datesToFill,
            List<PotdPlanningCandidate> candidates,
            List<FixedAssignment> fixedAssignments,
            Map<LocalDate, Long> historyByDate) {
    }

    public record PlannerResult(List<PlannedAssignment> assignments, String configurationHash) {
    }

    public record RankedCandidate(
            PotdPlanningCandidate candidate,
            double score,
            String tieSeed) {
    }
}
