package com.hrishabh.problemservice.dailychallenge.planner;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PotdPlanningCandidate;

public final class PotdCandidateScorer {

    private static final double[] RECENCY_DECAY = {1.0, 0.75, 0.55, 0.4, 0.3, 0.2, 0.1};

    private PotdCandidateScorer() {
    }

    public static double score(
            PotdPlanningCandidate candidate,
            LocalDate date,
            PotdSchedulerProperties properties,
            Map<PotdDifficulty, Integer> difficultyTargets,
            Map<PotdDifficulty, Integer> difficultyScheduled,
            NavigableMap<LocalDate, Long> scheduleState,
            Map<Long, PotdPlanningCandidate> candidatesById) {
        PotdSchedulerProperties.ScoringWeights w = properties.getScoring();

        double quality = clamp01(candidate.qualityNormalized());
        double difficultyDeficit = difficultyDeficitComponent(
                candidate.difficulty(), difficultyTargets, difficultyScheduled);
        double topicDeficit = 0.5;
        if (candidate.primaryTagId() != null) {
            topicDeficit = 0.5;
        }
        double freshness = freshness(candidate.questionId(), date, scheduleState);
        double neverUsed = candidate.neverScheduled() ? 1.0 : 0.0;
        double similarity = recentTagSimilarity(candidate, date, scheduleState, candidatesById);
        double topicRecency = primaryTopicRecency(candidate, date, scheduleState);
        double streakPenalty = 0.0;
        double overTarget = overTargetPenalty(candidate.difficulty(), difficultyTargets, difficultyScheduled);

        return w.getQuality() * quality
                + w.getDifficultyDeficit() * difficultyDeficit
                + w.getPrimaryTopicDeficit() * topicDeficit
                + w.getFreshness() * freshness
                + w.getNeverUsed() * neverUsed
                - w.getRecentTagSimilarity() * similarity
                - w.getPrimaryTopicRecency() * topicRecency
                - w.getDifficultyStreak() * streakPenalty
                - w.getOverTarget() * overTarget;
    }

    private static double difficultyDeficitComponent(
            PotdDifficulty difficulty,
            Map<PotdDifficulty, Integer> targets,
            Map<PotdDifficulty, Integer> scheduled) {
        int target = targets.getOrDefault(difficulty, 0);
        int have = scheduled.getOrDefault(difficulty, 0);
        if (target <= have) {
            return 0.0;
        }
        return clamp01((double) (target - have) / Math.max(1, target));
    }

    private static double overTargetPenalty(
            PotdDifficulty difficulty,
            Map<PotdDifficulty, Integer> targets,
            Map<PotdDifficulty, Integer> scheduled) {
        int target = targets.getOrDefault(difficulty, 0);
        int have = scheduled.getOrDefault(difficulty, 0);
        if (have <= target) {
            return 0.0;
        }
        return clamp01((double) (have - target) / Math.max(1, target));
    }

    private static double freshness(long questionId, LocalDate date, NavigableMap<LocalDate, Long> scheduleState) {
        LocalDate lastUsed = null;
        for (Map.Entry<LocalDate, Long> entry : scheduleState.entrySet()) {
            if (entry.getValue() == questionId && entry.getKey().isBefore(date)) {
                lastUsed = entry.getKey();
            }
        }
        if (lastUsed == null) {
            return 1.0;
        }
        long days = ChronoUnit.DAYS.between(lastUsed, date);
        return clamp01(days / 90.0);
    }

    private static double primaryTopicRecency(
            PotdPlanningCandidate candidate,
            LocalDate date,
            NavigableMap<LocalDate, Long> scheduleState) {
        if (candidate.primaryTagId() == null) {
            return 0.0;
        }
        List<LocalDate> recent = scheduleState.headMap(date, false).descendingKeySet().stream()
                .limit(3)
                .toList();
        for (LocalDate d : recent) {
            Long qid = scheduleState.get(d);
            if (qid != null && qid.equals(candidate.questionId())) {
                return 1.0;
            }
        }
        return 0.0;
    }

    private static double recentTagSimilarity(
            PotdPlanningCandidate candidate,
            LocalDate date,
            NavigableMap<LocalDate, Long> scheduleState,
            Map<Long, PotdPlanningCandidate> candidatesById) {
        List<LocalDate> recentDates = scheduleState.headMap(date, false).descendingKeySet().stream()
                .limit(RECENCY_DECAY.length)
                .sorted(Comparator.reverseOrder())
                .toList();
        double penalty = 0.0;
        for (int i = 0; i < recentDates.size(); i++) {
            Long otherId = scheduleState.get(recentDates.get(i));
            if (otherId == null) {
                continue;
            }
            PotdPlanningCandidate other = candidatesById.get(otherId);
            if (other == null) {
                continue;
            }
            double jaccard = weightedJaccard(candidate.tagWeights(), other.tagWeights());
            penalty += RECENCY_DECAY[i] * jaccard;
        }
        return clamp01(penalty);
    }

    static double weightedJaccard(Map<Long, Double> a, Map<Long, Double> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0.0;
        }
        double intersection = 0.0;
        double union = 0.0;
        for (Map.Entry<Long, Double> e : a.entrySet()) {
            double bw = b.getOrDefault(e.getKey(), 0.0);
            intersection += Math.min(e.getValue(), bw);
            union += Math.max(e.getValue(), bw);
        }
        for (Map.Entry<Long, Double> e : b.entrySet()) {
            if (!a.containsKey(e.getKey())) {
                union += e.getValue();
            }
        }
        if (union <= 0.0) {
            return 0.0;
        }
        return intersection / union;
    }

    private static double clamp01(double v) {
        if (v < 0.0) {
            return 0.0;
        }
        if (v > 1.0) {
            return 1.0;
        }
        return v;
    }

    public static Map<PotdDifficulty, Integer> emptyDifficultyCounts() {
        EnumMap<PotdDifficulty, Integer> map = new EnumMap<>(PotdDifficulty.class);
        map.put(PotdDifficulty.EASY, 0);
        map.put(PotdDifficulty.MEDIUM, 0);
        map.put(PotdDifficulty.HARD, 0);
        return map;
    }

    public static NavigableMap<LocalDate, Long> buildInitialState(
            Map<LocalDate, Long> history,
            List<PotdPlanningModels.FixedAssignment> fixed) {
        NavigableMap<LocalDate, Long> state = new TreeMap<>();
        if (history != null) {
            state.putAll(history);
        }
        if (fixed != null) {
            for (PotdPlanningModels.FixedAssignment fa : fixed) {
                state.put(fa.date(), fa.questionId());
            }
        }
        return state;
    }

    public static boolean passesHardConstraints(
            PotdPlanningCandidate candidate,
            LocalDate date,
            NavigableMap<LocalDate, Long> state,
            PotdSchedulerProperties properties,
            Map<Long, PotdPlanningCandidate> candidatesById) {
        if (candidate.difficulty() == PotdDifficulty.UNKNOWN) {
            return false;
        }
        LocalDate prev = date.minusDays(1);
        if (state.containsKey(prev) && state.get(prev) == candidate.questionId()) {
            return false;
        }
        for (Map.Entry<LocalDate, Long> entry : state.entrySet()) {
            if (entry.getValue() != candidate.questionId()) {
                continue;
            }
            long days = Math.abs(ChronoUnit.DAYS.between(entry.getKey(), date));
            if (days > 0 && days < properties.getEmergencyMinCooldownDays()) {
                return false;
            }
            if (days > 0 && days < properties.getExactCooldownDays()) {
                return false;
            }
        }
        String family = candidate.familyKey();
        if (family != null && !family.isBlank()) {
            for (Map.Entry<LocalDate, Long> entry : state.entrySet()) {
                if (entry.getKey().equals(date)) {
                    continue;
                }
                long days = Math.abs(ChronoUnit.DAYS.between(entry.getKey(), date));
                if (days > 0 && days < properties.getFamilyCooldownDays()) {
                    PotdPlanningCandidate other = candidatesById.get(entry.getValue());
                    if (other != null && family.equals(other.familyKey())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<PotdPlanningModels.RankedCandidate> rankCandidates(
            List<PotdPlanningCandidate> eligible,
            LocalDate date,
            PotdSchedulerProperties properties,
            String configurationHash,
            LocalDate planningStart,
            LocalDate planningEnd,
            Map<PotdDifficulty, Integer> difficultyTargets,
            Map<PotdDifficulty, Integer> difficultyScheduled,
            NavigableMap<LocalDate, Long> scheduleState,
            Map<Long, PotdPlanningCandidate> candidatesById) {
        List<PotdPlanningModels.RankedCandidate> ranked = new ArrayList<>();
        for (PotdPlanningCandidate c : eligible) {
            double score = score(
                    c, date, properties, difficultyTargets, difficultyScheduled, scheduleState, candidatesById);
            String seed = PotdTieBreaker.deterministicSeed(
                    properties.getSchedulerVersion(),
                    configurationHash,
                    planningStart,
                    planningEnd,
                    date,
                    c.questionId());
            ranked.add(new PotdPlanningModels.RankedCandidate(c, score, seed));
        }
        ranked.sort((a, b) -> {
            int scoreCmp = Double.compare(b.score(), a.score());
            if (scoreCmp != 0) {
                return scoreCmp;
            }
            return PotdTieBreaker.compareAscending(
                    a.tieSeed(), a.candidate().questionId(),
                    b.tieSeed(), b.candidate().questionId());
        });
        return ranked;
    }
}
