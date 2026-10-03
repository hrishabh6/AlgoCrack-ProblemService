package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dto.AcceptedSubmissionDayDto;
import com.hrishabh.problemservice.dto.StreakDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

/** Calculates streaks only from POTDs accepted on their scheduled challenge date. */
public final class PotdStreakCalculator {

    private PotdStreakCalculator() {
    }

    public static StreakDto calculate(
            Map<LocalDate, Long> scheduledQuestionByDate,
            Collection<AcceptedSubmissionDayDto> acceptedSubmissions,
            LocalDate today) {
        TreeSet<LocalDate> solvedPotdDays = new TreeSet<>();
        for (AcceptedSubmissionDayDto submission : acceptedSubmissions) {
            LocalDate date = submission.getSubmissionDate();
            if (date != null
                    && !date.isAfter(today)
                    && submission.getQuestionId() != null
                    && submission.getQuestionId().equals(scheduledQuestionByDate.get(date))) {
                solvedPotdDays.add(date);
            }
        }

        if (solvedPotdDays.isEmpty()) {
            return StreakDto.builder().build();
        }

        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate day : solvedPotdDays) {
            run = previous != null && previous.plusDays(1).equals(day) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = day;
        }

        LocalDate last = solvedPotdDays.last();
        boolean activeToday = last.equals(today);
        int current = 0;
        if (activeToday || last.equals(today.minusDays(1))) {
            for (LocalDate cursor = last; solvedPotdDays.contains(cursor); cursor = cursor.minusDays(1)) {
                current++;
            }
        }

        return StreakDto.builder()
                .currentStreak(current)
                .longestStreak(longest)
                .activeToday(activeToday)
                .lastActiveDate(last.toString())
                .totalActiveDays(solvedPotdDays.size())
                .build();
    }
}
