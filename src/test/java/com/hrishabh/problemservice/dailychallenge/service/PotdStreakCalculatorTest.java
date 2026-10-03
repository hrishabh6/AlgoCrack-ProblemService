package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dto.AcceptedSubmissionDayDto;
import com.hrishabh.problemservice.dto.StreakDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PotdStreakCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 3);

    @Test
    void countsOnlyAcceptedSubmissionsMatchingThatDaysPotd() {
        Map<LocalDate, Long> schedule = Map.of(
                TODAY.minusDays(2), 10L,
                TODAY.minusDays(1), 20L,
                TODAY, 30L);
        List<AcceptedSubmissionDayDto> accepted = List.of(
                new AcceptedSubmissionDayDto(10L, TODAY.minusDays(2)),
                new AcceptedSubmissionDayDto(999L, TODAY.minusDays(1)),
                new AcceptedSubmissionDayDto(20L, TODAY),
                new AcceptedSubmissionDayDto(30L, TODAY));

        StreakDto streak = PotdStreakCalculator.calculate(schedule, accepted, TODAY);

        assertThat(streak.getCurrentStreak()).isEqualTo(1);
        assertThat(streak.getLongestStreak()).isEqualTo(1);
        assertThat(streak.getTotalActiveDays()).isEqualTo(2);
        assertThat(streak.isActiveToday()).isTrue();
    }

    @Test
    void countsConsecutivePotdSolvesAndIgnoresDuplicates() {
        Map<LocalDate, Long> schedule = Map.of(
                TODAY.minusDays(2), 10L,
                TODAY.minusDays(1), 20L,
                TODAY, 30L);
        List<AcceptedSubmissionDayDto> accepted = List.of(
                new AcceptedSubmissionDayDto(10L, TODAY.minusDays(2)),
                new AcceptedSubmissionDayDto(20L, TODAY.minusDays(1)),
                new AcceptedSubmissionDayDto(30L, TODAY),
                new AcceptedSubmissionDayDto(30L, TODAY));

        StreakDto streak = PotdStreakCalculator.calculate(schedule, accepted, TODAY);

        assertThat(streak.getCurrentStreak()).isEqualTo(3);
        assertThat(streak.getLongestStreak()).isEqualTo(3);
        assertThat(streak.getTotalActiveDays()).isEqualTo(3);
    }

    @Test
    void streakRemainsAliveWhenYesterdayWasSolved() {
        Map<LocalDate, Long> schedule = Map.of(TODAY.minusDays(1), 20L, TODAY, 30L);

        StreakDto streak = PotdStreakCalculator.calculate(
                schedule, List.of(new AcceptedSubmissionDayDto(20L, TODAY.minusDays(1))), TODAY);

        assertThat(streak.getCurrentStreak()).isEqualTo(1);
        assertThat(streak.isActiveToday()).isFalse();
    }
}
