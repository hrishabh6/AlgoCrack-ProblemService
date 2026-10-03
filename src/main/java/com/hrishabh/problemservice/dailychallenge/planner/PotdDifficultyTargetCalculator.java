package com.hrishabh.problemservice.dailychallenge.planner;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;

import java.util.EnumMap;
import java.util.Map;

public final class PotdDifficultyTargetCalculator {

    private PotdDifficultyTargetCalculator() {
    }

    /**
     * Largest-remainder allocation of integer difficulty slots for a planning window.
     */
    public static Map<PotdDifficulty, Integer> targets(int windowSize, PotdSchedulerProperties.DifficultyTargets ratios) {
        if (windowSize <= 0) {
            return Map.of(PotdDifficulty.EASY, 0, PotdDifficulty.MEDIUM, 0, PotdDifficulty.HARD, 0);
        }
        double easyShare = ratios.getEasyRatio() * windowSize;
        double mediumShare = ratios.getMediumRatio() * windowSize;
        double hardShare = ratios.getHardRatio() * windowSize;

        int easy = (int) Math.floor(easyShare);
        int medium = (int) Math.floor(mediumShare);
        int hard = (int) Math.floor(hardShare);
        int assigned = easy + medium + hard;
        int remainder = windowSize - assigned;

        double[] fractional = {
                easyShare - easy,
                mediumShare - medium,
                hardShare - hard
        };
        PotdDifficulty[] order = {PotdDifficulty.EASY, PotdDifficulty.MEDIUM, PotdDifficulty.HARD};
        while (remainder > 0) {
            int bestIdx = 0;
            for (int i = 1; i < fractional.length; i++) {
                if (fractional[i] > fractional[bestIdx]) {
                    bestIdx = i;
                }
            }
            fractional[bestIdx] = -1;
            switch (order[bestIdx]) {
                case EASY -> easy++;
                case MEDIUM -> medium++;
                case HARD -> hard++;
                default -> {
                }
            }
            remainder--;
        }

        EnumMap<PotdDifficulty, Integer> result = new EnumMap<>(PotdDifficulty.class);
        result.put(PotdDifficulty.EASY, easy);
        result.put(PotdDifficulty.MEDIUM, medium);
        result.put(PotdDifficulty.HARD, hard);
        return result;
    }
}
