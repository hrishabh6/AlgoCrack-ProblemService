package com.hrishabh.problemservice.dailychallenge.planner;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PotdDifficultyTargetCalculatorTest {

    @Test
    void largestRemainder_allocatesFullWindow() {
        PotdSchedulerProperties.DifficultyTargets ratios = new PotdSchedulerProperties.DifficultyTargets();
        ratios.setEasyRatio(0.20);
        ratios.setMediumRatio(0.60);
        ratios.setHardRatio(0.20);

        Map<PotdDifficulty, Integer> targets = PotdDifficultyTargetCalculator.targets(30, ratios);

        int sum = targets.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(sum).isEqualTo(30);
        assertThat(targets.get(PotdDifficulty.EASY)).isEqualTo(6);
        assertThat(targets.get(PotdDifficulty.MEDIUM)).isEqualTo(18);
        assertThat(targets.get(PotdDifficulty.HARD)).isEqualTo(6);
    }
}
