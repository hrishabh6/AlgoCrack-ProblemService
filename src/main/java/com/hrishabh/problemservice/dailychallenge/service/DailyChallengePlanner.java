package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;
import com.hrishabh.problemservice.dailychallenge.planner.PotdCandidateScorer;
import com.hrishabh.problemservice.dailychallenge.planner.PotdConfigurationHasher;
import com.hrishabh.problemservice.dailychallenge.planner.PotdDifficulty;
import com.hrishabh.problemservice.dailychallenge.planner.PotdDifficultyTargetCalculator;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PlannedAssignment;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PlannerRequest;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PlannerResult;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PotdPlanningCandidate;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.RankedCandidate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class DailyChallengePlanner {

    private final PotdSchedulerProperties properties;

    public DailyChallengePlanner(PotdSchedulerProperties properties) {
        this.properties = properties;
    }

    public PlannerResult plan(PlannerRequest request) {
        String configurationHash = PotdConfigurationHasher.hash(properties);
        int windowSize = (int) (request.planningEnd().toEpochDay() - request.planningStart().toEpochDay()) + 1;
        Map<PotdDifficulty, Integer> difficultyTargets =
                PotdDifficultyTargetCalculator.targets(windowSize, properties.getDifficulty());

        Map<Long, PotdPlanningCandidate> candidatesById = new HashMap<>();
        for (PotdPlanningCandidate c : request.candidates()) {
            candidatesById.put(c.questionId(), c);
        }

        NavigableMap<LocalDate, Long> scheduleState =
                PotdCandidateScorer.buildInitialState(request.historyByDate(), request.fixedAssignments());
        Map<PotdDifficulty, Integer> difficultyScheduled = countDifficulties(scheduleState, candidatesById);

        List<PlannedAssignment> planned = new ArrayList<>();
        List<LocalDate> dates = request.datesToFill().stream().sorted().toList();

        for (LocalDate date : dates) {
            if (scheduleState.containsKey(date)) {
                continue;
            }
            List<PotdPlanningCandidate> eligible = request.candidates().stream()
                    .filter(c -> PotdCandidateScorer.passesHardConstraints(
                            c, date, scheduleState, properties, candidatesById))
                    .toList();
            if (eligible.isEmpty()) {
                throw new IllegalStateException("No eligible candidate for date " + date);
            }
            List<RankedCandidate> ranked = PotdCandidateScorer.rankCandidates(
                    eligible,
                    date,
                    properties,
                    configurationHash,
                    request.planningStart(),
                    request.planningEnd(),
                    difficultyTargets,
                    difficultyScheduled,
                    scheduleState,
                    candidatesById);
            RankedCandidate best = ranked.getFirst();
            scheduleState.put(date, best.candidate().questionId());
            incrementDifficulty(difficultyScheduled, best.candidate().difficulty());
            planned.add(new PlannedAssignment(
                    date,
                    best.candidate().questionId(),
                    BigDecimal.valueOf(best.score()).setScale(6, RoundingMode.HALF_UP),
                    best.tieSeed()));
        }
        return new PlannerResult(planned, configurationHash);
    }

    private static Map<PotdDifficulty, Integer> countDifficulties(
            NavigableMap<LocalDate, Long> scheduleState, Map<Long, PotdPlanningCandidate> candidatesById) {
        Map<PotdDifficulty, Integer> counts = PotdCandidateScorer.emptyDifficultyCounts();
        for (Long qid : scheduleState.values()) {
            PotdPlanningCandidate c = candidatesById.get(qid);
            if (c != null && c.difficulty() != PotdDifficulty.UNKNOWN) {
                incrementDifficulty(counts, c.difficulty());
            }
        }
        return counts;
    }

    private static void incrementDifficulty(Map<PotdDifficulty, Integer> counts, PotdDifficulty difficulty) {
        counts.put(difficulty, counts.getOrDefault(difficulty, 0) + 1);
    }
}
